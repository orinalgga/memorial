package com.memorial.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memorial.server.common.Access;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(properties = {"memorial.limits.writes-per-minute=1000", "memorial.admin-token=" + DeleteAndAdminTest.ADMIN})
@AutoConfigureMockMvc
class DeleteAndAdminTest {

    static final String ADMIN = "admin-token-for-tests-0123456789";
    static final String MINE = "ownerkeyofbomb1234567890";
    static final String THEIRS = "ownerkeyofhaneul12345678";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    ResultActions postJson(String url, Object body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)));
    }

    String id(ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void writersSeeTheirTagAndOnlyTheyCanDelete() throws Exception {
        String tag = Access.sha256(MINE).substring(0, 16);
        ResultActions written = postJson("/api/spaces/garden/letters",
                Map.of("to", "아빠", "message", "보고 싶어요", "name", "봄비", "visitorId", "visitor001", "ownerKey", MINE))
                .andExpect(jsonPath("$.owner").value(tag));
        String letter = id(written);

        // 남의 키, 키 없음, 틀린 관리자 토큰으로는 못 지운다.
        mvc.perform(delete("/api/letters/" + letter).header(Access.OWNER_HEADER, THEIRS)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/letters/" + letter)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/letters/" + letter).header(Access.ADMIN_HEADER, "wrong-token-wrong-token")).andExpect(status().isForbidden());

        String c1 = id(postJson("/api/letters/" + letter + "/comments", Map.of("text", "힘내요", "ownerKey", THEIRS)));
        postJson("/api/letters/" + letter + "/comments", Map.of("text", "고마워요", "parentId", c1, "ownerKey", MINE));
        String c2 = id(postJson("/api/letters/" + letter + "/comments", Map.of("text", "함께해요", "ownerKey", THEIRS)));

        // 댓글을 지우면 답글도 함께 지워지고 사연의 댓글 수가 줄어든다.
        mvc.perform(delete("/api/comments/" + c1).header(Access.OWNER_HEADER, MINE)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/comments/" + c1).header(Access.OWNER_HEADER, THEIRS)).andExpect(status().isNoContent());
        mvc.perform(get("/api/letters/" + letter + "/comments")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/spaces/garden/letters")).andExpect(jsonPath("$[?(@.id == '" + letter + "')].comments").value(1));

        // 관리자는 누구의 댓글이든 지운다.
        mvc.perform(delete("/api/comments/" + c2).header(Access.ADMIN_HEADER, ADMIN)).andExpect(status().isNoContent());

        mvc.perform(delete("/api/letters/" + letter).header(Access.OWNER_HEADER, MINE)).andExpect(status().isNoContent());
        mvc.perform(get("/api/spaces/garden/letters")).andExpect(jsonPath("$[?(@.id == '" + letter + "')]").isEmpty());

        // 비밀 키 없이 쓴 글은 관리자만 지울 수 있다.
        String chat = id(postJson("/api/spaces/garden/chat", Map.of("text", "안녕하세요", "visitorId", "visitor001")));
        mvc.perform(delete("/api/chat/" + chat).header(Access.OWNER_HEADER, MINE)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/chat/" + chat).header(Access.ADMIN_HEADER, ADMIN)).andExpect(status().isNoContent());
        String mine = id(postJson("/api/spaces/garden/chat", Map.of("text", "다녀갑니다", "ownerKey", MINE)));
        mvc.perform(delete("/api/chat/" + mine).header(Access.OWNER_HEADER, MINE)).andExpect(status().isNoContent());
    }

    @Test
    void memorialIsDeletedByItsMakerOrAnAdminWithEverythingInside() throws Exception {
        JsonNode created = json.readTree(postJson("/api/memorials", Map.of("name", "박영수")).andReturn().getResponse().getContentAsString());
        String mid = created.at("/memorial/id").asText();
        String token = created.get("editToken").asText();
        postJson("/api/spaces/" + mid + "/letters", Map.of("to", "할아버지", "message", "사랑해요")).andExpect(status().isCreated());
        postJson("/api/spaces/" + mid + "/offerings", Map.of("kind", "candle")).andExpect(status().isCreated());
        postJson("/api/spaces/" + mid + "/chat", Map.of("text", "안녕")).andExpect(status().isCreated());

        mvc.perform(delete("/api/memorials/" + mid)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/memorials/" + mid).header("X-Edit-Token", token)).andExpect(status().isNoContent());
        mvc.perform(get("/api/spaces/" + mid + "/letters")).andExpect(status().isNotFound());

        String other = json.readTree(postJson("/api/memorials", Map.of("name", "이말순")).andReturn().getResponse().getContentAsString())
                .at("/memorial/id").asText();
        mvc.perform(delete("/api/memorials/" + other).header(Access.ADMIN_HEADER, ADMIN)).andExpect(status().isNoContent());
    }

    @Test
    void adminCheck() throws Exception {
        mvc.perform(post("/api/admin/check").header(Access.ADMIN_HEADER, ADMIN)).andExpect(status().isNoContent());
        mvc.perform(post("/api/admin/check").header(Access.ADMIN_HEADER, "nope")).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/check")).andExpect(status().isForbidden());
    }
}
