package com.example.speechtotextdemo;


import com.example.speechtotextdemo.services.azurestt.AzureWsHandler;
import com.example.speechtotextdemo.services.googlestt.GoogleWsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {


    @Autowired
    AppConfiguration appConfiguration;
    @Autowired
    AzureWsHandler azureWsHandler;
    @Autowired
    GoogleWsHandler googleWsHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(googleWsHandler, "/ws/googlestt").setAllowedOrigins(appConfiguration.getAllowedOrigins());
        registry.addHandler(azureWsHandler, "/ws/azurestt").setAllowedOrigins(appConfiguration.getAllowedOrigins());
    }
}
