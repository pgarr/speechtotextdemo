package com.example.speechtotextdemo.services;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public interface ICognitiveServiceWsHandler {

    public void configureConnection(WebSocketSession session) throws ExecutionException, InterruptedException, IOException;
    public void handleAudioSample(ByteBuffer payload);
    public void closeConnection();
}
