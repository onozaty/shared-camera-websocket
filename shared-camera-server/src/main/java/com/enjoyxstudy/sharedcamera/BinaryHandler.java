package com.enjoyxstudy.sharedcamera;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

@Component
public class BinaryHandler extends BinaryWebSocketHandler {

    private ConcurrentHashMap<String, Set<WebSocketSession>> tokenSessionPool = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String token = getToken(session);

        session.setBinaryMessageSizeLimit(1024 * 1024); // 上限1MB
        
        tokenSessionPool.compute(token, (key, sessions) -> {

            if (sessions == null) {
                sessions = new CopyOnWriteArraySet<>();
            }
            sessions.add(session);

            return sessions;
        });
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {

        String token = getToken(session);

        for (WebSocketSession tokenSession : tokenSessionPool.get(token)) {
            tokenSession.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        String token = getToken(session);

        tokenSessionPool.compute(token, (key, sessions) -> {

            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessions = null;
            }

            return sessions;
        });
    }

    private String getToken(WebSocketSession session) {
        return session.getUri().getQuery();
    }
}
