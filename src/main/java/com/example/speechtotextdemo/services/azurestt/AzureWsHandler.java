package com.example.speechtotextdemo.services.azurestt;

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
public class AzureWsHandler extends BinaryWebSocketHandler {
    @Autowired
    AzureService azureService;

    private static final Logger logger = LoggerFactory.getLogger(AzureWsHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        azureService.configureConnection(session);
        logger.info("AZURE API - Connection established");
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        ByteBuffer payload = message.getPayload();
        logger.debug(String.format("AZURE API - %s", payload));
        azureService.handleAudioSample(payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        azureService.closeConnection();
        logger.info("AZURE API - Connection closed");
    }
}
