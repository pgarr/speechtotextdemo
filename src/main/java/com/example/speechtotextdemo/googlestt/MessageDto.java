package com.example.speechtotextdemo.googlestt;

public class MessageDto {
    private boolean isFinal;
    private String text;

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toJsonString() {
        return String.format("{\"final\": %s, \"text\" : \"%s\"}", isFinal, text);
    }
}
