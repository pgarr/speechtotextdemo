package com.example.speechtotextdemo.googlestt;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1p1beta1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;

@Component
public class GoogleWsHandler extends BinaryWebSocketHandler {
    private static final String LANGUAGE_CODE = "pl_PL";
    private static final Logger logger = LoggerFactory.getLogger(GoogleWsHandler.class);

    private SpeechClient client;
    private ClientStream<StreamingRecognizeRequest> clientStream;
    private StreamController referenceToStreamController;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Google API - Connection established");

        ResponseObserver<StreamingRecognizeResponse> responseObserver = new ResponseObserver<StreamingRecognizeResponse>() {
            @Override
            public void onStart(StreamController controller) {
                logger.debug("Google API - Observer onStart");

                referenceToStreamController = controller;
            }

            @Override
            public void onResponse(StreamingRecognizeResponse response) {
                logger.debug("Google API - Observer onResponse");

                StreamingRecognitionResult result = response.getResultsList().get(0);
                boolean isFinal = result.getIsFinal();
                String text = result.getAlternativesList().get(0).getTranscript();
                logger.info(String.format("GOOGLE API - message read: %s", text));

                MessageDto msg = new MessageDto();
                msg.setFinal(isFinal);
                msg.setText(text);


                try {
                    session.sendMessage(new TextMessage(msg.toJsonString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                logger.warn("Google API - Observer onError: " + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                logger.debug("Google API - Observer onComplete");
            }
        };

        client = SpeechClient.create();
        clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

        RecognitionConfig recognitionConfig =
                RecognitionConfig.newBuilder()
                        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                        .setLanguageCode(LANGUAGE_CODE)
                        .setSampleRateHertz(16000)
                        .build();

        StreamingRecognitionConfig streamingRecognitionConfig =
                StreamingRecognitionConfig.newBuilder()
                        .setConfig(recognitionConfig)
                        .setInterimResults(true)
                        .build();

        StreamingRecognizeRequest request =
                StreamingRecognizeRequest.newBuilder()
                        .setStreamingConfig(streamingRecognitionConfig)
                        .build(); // The first request in a streaming call has to be a config

        clientStream.send(request);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        ByteBuffer payload = message.getPayload();
        logger.debug("Google API - " + payload);

        StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder().setAudioContent(ByteString.copyFrom(payload)).build();
        clientStream.send(request);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("Google API - Connection closed");

        clientStream.closeSend();
        referenceToStreamController.cancel(); // remove Observer
        client.close();
    }
}