package com.enjoyxstudy.sharedcamera;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BinaryHandler extends BinaryWebSocketHandler {

    private CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        session.setBinaryMessageSizeLimit(1024 * 1024); // Limit 1MB
        sessions.add(session);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        sendMessage(session, message.getPayload());
    }

    private void sendMessage(WebSocketSession fromSession, ByteBuffer fromByteBuffer) throws IOException {

        byte[] fromIdBytes = fromSession.getId().getBytes(StandardCharsets.UTF_8);

        ByteBuffer toByteBuffer = ByteBuffer.allocate(4 + fromIdBytes.length + fromByteBuffer.remaining())
                .putInt(fromIdBytes.length)
                .put(fromIdBytes)
                .put(fromByteBuffer);
        toByteBuffer.flip();

        BinaryMessage toMessage = new BinaryMessage(toByteBuffer);

        for (WebSocketSession session : sessions) {
            try {
                synchronized (session) {
                    session.sendMessage(toMessage);
                }
            } catch (Exception e) {
                log.warn(String.format("Sending failed. Target session=[%s]", session), e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        sendMessage(session, ByteBuffer.allocate(0));
    }
}
