package com.memorial.server.live;

import java.util.Map;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** 저장이 끝난 뒤 그 공간을 보고 있는 화면들에 알린다. */
@Component
public class Events {

    private final SimpMessagingTemplate broker;

    public Events(SimpMessagingTemplate broker) {
        this.broker = broker;
    }

    public void space(String space, String type, Object data) {
        broker.convertAndSend("/topic/spaces/" + space + "/events", Map.of("type", type, "data", data));
    }

    public void memorial(Object data) {
        broker.convertAndSend("/topic/memorials", data);
    }
}
