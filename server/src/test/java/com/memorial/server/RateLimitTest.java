package com.memorial.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "memorial.limits.writes-per-minute=3")
@AutoConfigureMockMvc
class RateLimitTest {

    @Autowired
    MockMvc mvc;

    @Test
    void tooManyWritesFromOneAddressAreRefusedButReadsAreNot() throws Exception {
        String body = "{\"kind\":\"candle\",\"name\":\"a\"}";
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/api/spaces/garden/offerings").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated());
        }
        mvc.perform(post("/api/spaces/garden/offerings").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests());
        mvc.perform(get("/api/spaces/garden/offerings")).andExpect(status().isOk());
    }
}
