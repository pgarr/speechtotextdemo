package com.example.speechtotextdemo;


import com.example.speechtotextdemo.azurestt.AzureWsHandler;
import com.example.speechtotextdemo.googlestt.GoogleWsHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new GoogleWsHandler(), "/ws/googlestt");
        registry.addHandler(new AzureWsHandler(), "ws/azurestt");
    }
}
