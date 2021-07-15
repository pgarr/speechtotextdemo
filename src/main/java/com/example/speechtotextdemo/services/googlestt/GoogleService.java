package com.example.speechtotextdemo.services.googlestt;

import com.example.speechtotextdemo.services.ICognitiveServiceWsHandler;
import com.example.speechtotextdemo.services.MessageDto;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1p1beta1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

@Service
public class GoogleService implements ICognitiveServiceWsHandler {

    @Autowired
    private GoogleConfiguration configuration;

    private static final Logger logger = LoggerFactory.getLogger(GoogleService.class);

    private SpeechClient client;
    private ClientStream<StreamingRecognizeRequest> clientStream;
    private StreamController referenceToStreamController;

    @Override
    public void configureConnection(WebSocketSession session) throws ExecutionException, InterruptedException, IOException {

        ResponseObserver<StreamingRecognizeResponse> responseObserver = new ResponseObserver<StreamingRecognizeResponse>() {
            @Override
            public void onStart(StreamController controller) {
                logger.debug("GOOGLE API - Observer::onStart");

                referenceToStreamController = controller;
            }

            @Override
            public void onResponse(StreamingRecognizeResponse response) {
                logger.debug("GOOGLE API - Observer::onResponse");

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
                logger.warn(String.format("GOOGLE API - Observer::onError: %s", throwable.getMessage()));
            }

            @Override
            public void onComplete() {
                logger.debug("GOOGLE API - Observer::onComplete");
            }
        };

        CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream(configuration.getCredentialsFile())));
        SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
        client = SpeechClient.create(settings);
        clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

        prepareAndSendConfig();  // The first request in a streaming call has to be a config
    }

    private void prepareAndSendConfig() {
        RecognitionConfig recognitionConfig =
                RecognitionConfig.newBuilder()
                        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                        .setLanguageCode(configuration.getLanguageSpeechCode())
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
                        .build();

        clientStream.send(request);
    }

    @Override
    public void handleAudioSample(ByteBuffer payload) {
        logger.debug(String.format("GOOGLE API - %s", payload));
        StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder().setAudioContent(ByteString.copyFrom(payload)).build();
        clientStream.send(request);
    }

    @Override
    public void closeConnection() {
        clientStream.closeSend();
        referenceToStreamController.cancel(); // remove Observer
        client.close();
    }
}
