package com.example.speechtotextdemo.azurestt;

import com.example.speechtotextdemo.MessageDto;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioInputStream;
import com.microsoft.cognitiveservices.speech.audio.PushAudioInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AzureWsHandler extends BinaryWebSocketHandler {

    private static final String LANGUAGE_CODE = "pl-PL";
    private static final Logger logger = LoggerFactory.getLogger(AzureWsHandler.class);

    @Value("${AZURE_SPEECH_KEY}")
    private String speechKey;
    @Value("${AZURE_SPEECH_REGION}")
    private String speechRegion;

    private SpeechConfig config;
    private PushAudioInputStream pushStream;
    private AudioConfig audioInput;
    private SpeechRecognizer recognizer;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Azure API - Connection established");

        config = SpeechConfig.fromSubscription(speechKey, speechRegion);
        config.setSpeechRecognitionLanguage(LANGUAGE_CODE);
        pushStream = AudioInputStream.createPushStream();
        audioInput = AudioConfig.fromStreamInput(pushStream);
        recognizer = new SpeechRecognizer(config, audioInput);
        {
            recognizer.recognizing.addEventListener((s, e) -> {
                logger.debug("Azure API - recognizer::recognizing event");

                String text = e.getResult().getText();

                logger.info(String.format("GOOGLE API - message read: %s", text));

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
                logger.debug("Azure API - recognizer::recognized event");

                String text = e.getResult().getText();

                logger.info(String.format("GOOGLE API - message read: %s", text));

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
                logger.warn("Azure API - recognizer::canceled event");
                logger.warn(String.format("Canceling reason: %s", e.getReason()));
            });

            recognizer.sessionStarted.addEventListener((s, e) -> logger.debug("Azure API - recognizer::sessionStarted event"));

            recognizer.sessionStopped.addEventListener((s, e) -> logger.debug("Azure API - recognizer::sessionStopped event"));
        }
        recognizer.startContinuousRecognitionAsync().get();
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        ByteBuffer payload = message.getPayload();
        logger.debug("Azure API - " + payload);

        pushStream.write(payload.array());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Azure API - Connection closed");

        pushStream.close();
        recognizer.stopContinuousRecognitionAsync().get();
        config.close();
        audioInput.close();
        recognizer.close();
    }
}
