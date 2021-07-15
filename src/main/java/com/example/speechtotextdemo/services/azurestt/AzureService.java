package com.example.speechtotextdemo.services.azurestt;

import com.example.speechtotextdemo.services.ICognitiveServiceWsHandler;
import com.example.speechtotextdemo.services.MessageDto;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioInputStream;
import com.microsoft.cognitiveservices.speech.audio.PushAudioInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

@Service
public class AzureService implements ICognitiveServiceWsHandler {

    @Autowired
    private AzureConfiguration azureConfiguration;

    private static final Logger logger = LoggerFactory.getLogger(AzureService.class);

    private SpeechConfig config;
    private PushAudioInputStream pushStream;
    private AudioConfig audioInput;
    private SpeechRecognizer recognizer;


    @Override
    public void configureConnection(WebSocketSession session) throws ExecutionException, InterruptedException {
        config = SpeechConfig.fromSubscription(azureConfiguration.getSpeechKey(), azureConfiguration.getSpeechRegion());
        config.setSpeechRecognitionLanguage(azureConfiguration.getLanguageSpeechCode());
        pushStream = AudioInputStream.createPushStream();
        audioInput = AudioConfig.fromStreamInput(pushStream);
        recognizer = new SpeechRecognizer(config, audioInput);
        setUpListeners(session);

        recognizer.startContinuousRecognitionAsync().get();
    }

    private void setUpListeners(WebSocketSession session) {
        recognizer.recognizing.addEventListener((s, e) -> {
            logger.debug("AZURE API - recognizer::recognizing event");

            String text = e.getResult().getText();

            logger.info(String.format("AZURE API - message read: %s", text));

            MessageDto msg = new MessageDto();
            msg.setFinal(false);
            msg.setText(text);

            try {
                session.sendMessage(new TextMessage(msg.toJsonString()));
            } catch (IOException error) {
                error.printStackTrace();
            }
        });

        recognizer.recognized.addEventListener((s, e) -> {
            logger.debug("AZURE API - recognizer::recognized event");

            String text = e.getResult().getText();

            logger.info(String.format("AZURE API - message read: %s", text));

            MessageDto msg = new MessageDto();
            msg.setFinal(true);
            msg.setText(text);

            try {
                session.sendMessage(new TextMessage(msg.toJsonString()));
            } catch (IOException error) {
                error.printStackTrace();
            }
        });

        recognizer.canceled.addEventListener((s, e) -> {
            logger.warn("AZURE API - recognizer::canceled event");
            logger.warn(String.format("Canceling reason: %s", e.getReason()));
        });

        recognizer.sessionStarted.addEventListener((s, e) -> logger.debug("AZURE API - recognizer::sessionStarted event"));

        recognizer.sessionStopped.addEventListener((s, e) -> logger.debug("AZURE API - recognizer::sessionStopped event"));
    }

    @Override
    public void handleAudioSample(ByteBuffer payload) {
        pushStream.write(payload.array());
    }

    @Override
    public void closeConnection() {
        pushStream.close();
        try {
            recognizer.stopContinuousRecognitionAsync().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        config.close();
        audioInput.close();
        recognizer.close();
    }
}
