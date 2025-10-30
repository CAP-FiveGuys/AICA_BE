package com.aica.aivoca.pronunciation.handler;

import com.aica.aivoca.pronunciation.service.PronunciationAssessmentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.PushAudioInputStream;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 발음 평가를 위한 WebSocket 요청을 처리하는 핸들러
 */
@Component
public class PronunciationWebSocketHandler extends AbstractWebSocketHandler {

    private final PronunciationAssessmentService pronunciationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, SpeechRecognizer> recognizers = new ConcurrentHashMap<>();
    private final Map<String, PushAudioInputStream> audioStreams = new ConcurrentHashMap<>();

    public PronunciationWebSocketHandler(PronunciationAssessmentService pronunciationService) {
        this.pronunciationService = pronunciationService;
    }

    /**
     * 클라이언트와 WebSocket 연결이 성공적으로 되었을 때 호출
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket 연결 수립: " + session.getId());
        // 새 연결을 위해 오디오 데이터를 받을 스트림을 생성하고 맵에 저장합니다.
        PushAudioInputStream stream = PushAudioInputStream.create();
        audioStreams.put(session.getId(), stream);
    }

    /**
     * 클라이언트로부터 텍스트 메시지를 받았을 때 호출
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JsonNode jsonNode = objectMapper.readTree(payload);
        String referenceText = jsonNode.get("text").asText();

        System.out.println("기준 텍스트 수신 (" + session.getId() + "): " + referenceText);

        PushAudioInputStream stream = audioStreams.get(session.getId());
        if (stream != null && referenceText != null && !referenceText.isEmpty()) {
            // 서비스에 평가 시작을 요청하고, 생성된 recognizer를 맵에 저장
            SpeechRecognizer recognizer = pronunciationService.startContinuousAssessment(session, referenceText, stream);
            recognizers.put(session.getId(), recognizer);
        }
    }

    /**
     * 클라이언트로부터 바이너리 메시지를 받았을 때 호출
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        PushAudioInputStream stream = audioStreams.get(session.getId());
        if (stream != null) {
            byte[] payload = message.getPayload().array();
            stream.write(payload);
        }
    }

    /**
     * 클라이언트와의 WebSocket 연결이 닫혔을 때 호출
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("WebSocket 연결 종료: " + session.getId() + " 상태: " + status);

        SpeechRecognizer recognizer = recognizers.remove(session.getId());
        if (recognizer != null) {
            pronunciationService.stopContinuousAssessment(recognizer);
        }

        PushAudioInputStream stream = audioStreams.remove(session.getId());
        if (stream != null) {
            stream.close();
        }
    }
}