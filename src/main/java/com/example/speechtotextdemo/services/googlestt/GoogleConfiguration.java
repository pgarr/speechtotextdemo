package com.example.speechtotextdemo.services.googlestt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("google")
public class GoogleConfiguration {

    private String languageSpeechCode;

    public String getLanguageSpeechCode() {
        return languageSpeechCode;
    }

    public void setLanguageSpeechCode(String languageSpeechCode) {
        this.languageSpeechCode = languageSpeechCode;
    }
}
