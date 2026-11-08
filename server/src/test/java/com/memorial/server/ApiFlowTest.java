package com.memorial.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(properties = "memorial.limits.writes-per-minute=1000")
@AutoConfigureMockMvc
class ApiFlowTest {

    // 1x1 투명 PNG
    static final String PNG = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    ResultActions postJson(String url, Object body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)));
    }

    JsonNode body(ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsString());
    }

    @Test
    void storiesHeartsAndThreadedComments() throws Exception {
        JsonNode letter = body(postJson("/api/spaces/garden/letters",
                Map.of("to", "엄마", "message", "오늘 첫눈이 왔어요.", "name", "봄비", "visitorId", "visitor001"))
                .andExpect(status().isCreated()));
        String id = letter.get("id").asText();

        // 같은 방문자의 공감은 한 번만 센다.
        postJson("/api/letters/" + id + "/hearts", Map.of("visitorId", "visitor002")).andExpect(jsonPath("$.hearts").value(1));
        postJson("/api/letters/" + id + "/hearts", Map.of("visitorId", "visitor002"))
                .andExpect(jsonPath("$.hearts").value(1)).andExpect(jsonPath("$.added").value(false));
        postJson("/api/letters/" + id + "/hearts", Map.of("visitorId", "visitor003")).andExpect(jsonPath("$.hearts").value(2));

        String c1 = body(postJson("/api/letters/" + id + "/comments", Map.of("text", "함께 기억할게요", "name", "하늘", "visitorId", "visitor002")))
                .get("id").asText();
        String reply = body(postJson("/api/letters/" + id + "/comments",
                Map.of("text", "고마워요", "name", "봄비", "visitorId", "visitor001", "parentId", c1))).get("id").asText();
        // 답글에 단 답글은 원래 댓글 아래로 모인다.
        postJson("/api/letters/" + id + "/comments", Map.of("text", "저도요", "name", "별", "visitorId", "visitor003", "parentId", reply))
                .andExpect(jsonPath("$.parentId").value(c1));

        mvc.perform(get("/api/letters/" + id + "/comments")).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/api/spaces/garden/letters"))
                .andExpect(jsonPath("$[?(@.id == '" + id + "')].hearts").value(2))
                .andExpect(jsonPath("$[?(@.id == '" + id + "')].comments").value(3));
    }

    @Test
    void personalSpaceKeepsItsOwnLettersOfferingsAndChat() throws Exception {
        JsonNode created = body(postJson("/api/memorials",
                Map.of("name", "김순자", "years", "1948 – 2025", "intro", "된장찌개를 제일 잘 끓이던 할머니", "photo", PNG, "createdBy", "봄비"))
                .andExpect(status().isCreated()));
        String mid = created.at("/memorial/id").asText();
        String token = created.get("editToken").asText();
        assertThat(created.at("/memorial/hasPhoto").asBoolean()).isTrue();

        mvc.perform(get("/api/memorials/" + mid + "/photo"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));

        postJson("/api/spaces/" + mid + "/offerings", Map.of("kind", "candle", "name", "봄비")).andExpect(status().isCreated());
        postJson("/api/spaces/" + mid + "/offerings", Map.of("kind", "flower", "name", "봄비")).andExpect(status().isCreated());
        postJson("/api/spaces/" + mid + "/offerings", Map.of("kind", "bomb")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/spaces/" + mid + "/offerings"))
                .andExpect(jsonPath("$.candles").value(1)).andExpect(jsonPath("$.flowers").value(1));

        postJson("/api/spaces/" + mid + "/chat", Map.of("text", "할머니 생각나서 왔어요", "name", "봄비", "visitorId", "visitor001"));
        mvc.perform(get("/api/spaces/" + mid + "/chat")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/spaces/garden/chat")).andExpect(jsonPath("$[?(@.text == '할머니 생각나서 왔어요')]").isEmpty());

        // 만든 사람만 고칠 수 있다.
        mvc.perform(put("/api/memorials/" + mid).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("name", "다른 이름"))))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/memorials/" + mid).header("X-Edit-Token", token).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("name", "김순자", "intro", "우리 할머니"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.intro").value("우리 할머니"));

        mvc.perform(get("/api/memorials")).andExpect(jsonPath("$[0].id").value(mid));
    }

    @Test
    void unknownSpaceAndBadInputAreRejected() throws Exception {
        postJson("/api/spaces/nosuchspace1/letters", Map.of("to", "a", "message", "b")).andExpect(status().isNotFound());
        postJson("/api/spaces/garden/letters", Map.of("to", "", "message", "b"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        postJson("/api/memorials", Map.of("name", "이름", "photo", "data:text/html;base64,PGgxPg=="))
                .andExpect(status().isBadRequest());
    }

    @Test
    void avatarFaceRoundTrip() throws Exception {
        mvc.perform(put("/api/avatars/visitor009/face").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("image", PNG))))
                .andExpect(status().isNoContent());
        byte[] png = mvc.perform(get("/api/avatars/visitor009/face")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(png).isEqualTo(Base64.getDecoder().decode(PNG.substring(PNG.indexOf(',') + 1)));
        mvc.perform(get("/api/avatars/NOT_VALID/face")).andExpect(status().isBadRequest());
    }
}
