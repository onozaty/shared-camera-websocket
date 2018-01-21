package com.enjoyxstudy.sharedcamera.client;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ImageWebSocketHandler extends BinaryWebSocketHandler {

    @Value("${websocket.uri}")
    private String webSocketUri;

    private WebSocketSession session;

    @Scheduled(fixedDelay = 10 * 1000)
    public void confirmConnection() {

        if (!isConnected()) {
            connect();
        }
    }

    private void connect() {

        StandardWebSocketClient client = new StandardWebSocketClient();
        client.doHandshake(this, webSocketUri).addCallback(
                new ListenableFutureCallback<WebSocketSession>() {
                    @Override
                    public void onSuccess(WebSocketSession webSocketSession) {
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        log.info("Connection failed.", e);
                    }
                });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        log.info("Connected.");

        this.session = session;
        this.session.setBinaryMessageSizeLimit(1024 * 1024); // Limit 1MB
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Closed. " + status.toString());
    }

    public void sendMessage(byte[] imageBytes) throws IOException {

        BinaryMessage message = new BinaryMessage(imageBytes);

        try {
            session.sendMessage(message);
        } catch (Exception e) {
            log.warn("Sending failed.", e);
        }
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }
}
