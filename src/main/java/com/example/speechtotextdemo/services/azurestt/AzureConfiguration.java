package com.example.speechtotextdemo.services.azurestt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("azure")
public class AzureConfiguration {

    private String speechKey;
    private String speechRegion;
    private String languageSpeechCode;

    public String getSpeechKey() {
        return speechKey;
    }

    public void setSpeechKey(String speechKey) {
        this.speechKey = speechKey;
    }

    public String getSpeechRegion() {
        return speechRegion;
    }

    public void setSpeechRegion(String speechRegion) {
        this.speechRegion = speechRegion;
    }

    public String getLanguageSpeechCode() {
        return languageSpeechCode;
    }

    public void setLanguageSpeechCode(String languageSpeechCode) {
        this.languageSpeechCode = languageSpeechCode;
    }
}
