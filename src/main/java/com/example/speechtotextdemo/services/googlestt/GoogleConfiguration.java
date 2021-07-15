package com.example.speechtotextdemo.services.googlestt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("google")
public class GoogleConfiguration {

    private String languageSpeechCode;
    private String credentialsFile;

    public String getLanguageSpeechCode() {
        return languageSpeechCode;
    }

    public void setLanguageSpeechCode(String languageSpeechCode) {
        this.languageSpeechCode = languageSpeechCode;
    }

    public String getCredentialsFile() {
        return credentialsFile;
    }

    public void setCredentialsFile(String credentialsFile) {
        this.credentialsFile = credentialsFile;
    }
}
