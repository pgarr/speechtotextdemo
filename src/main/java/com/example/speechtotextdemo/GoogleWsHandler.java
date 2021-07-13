package com.example.speechtotextdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Component
public class GoogleWsHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GoogleWsHandler.class);

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        logger.info(message.getPayload());
        session.sendMessage(new TextMessage("Got it!"));

    }
}