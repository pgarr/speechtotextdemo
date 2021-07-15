package com.example.speechtotextdemo.services.googlestt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.nio.ByteBuffer;

@Component
public class GoogleWsHandler extends BinaryWebSocketHandler {
    @Autowired
    GoogleService googleService;

    private static final Logger logger = LoggerFactory.getLogger(GoogleWsHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        googleService.configureConnection(session);
        logger.info("GOOGLE API - Connection established");
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        ByteBuffer payload = message.getPayload();
        logger.debug(String.format("GOOGLE API - %s", payload));
        googleService.handleAudioSample(payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        googleService.closeConnection();
        logger.info("GOOGLE API - Connection closed");
    }
}