package com.aica.aivoca.sentence.controller;

import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.sentence.dto.SentenceRequestDto;
import com.aica.aivoca.sentence.dto.SentenceResponseDto;
import com.aica.aivoca.sentence.service.addSentenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/sentence")
@RequiredArgsConstructor
public class SentenceController {

    private final addSentenceService sentenceService;

    @PostMapping
    public ResponseEntity<SuccessStatusResponse<SentenceResponseDto>> addSentence(@RequestBody SentenceRequestDto requestDto) {
        SuccessStatusResponse<SentenceResponseDto> response = sentenceService.addSentence(requestDto);
        return ResponseEntity.status(response.code()).body(response);
    }
}