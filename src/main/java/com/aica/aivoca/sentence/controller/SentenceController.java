package com.aica.aivoca.sentence.controller;

import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.sentence.dto.SentenceGetResponseDto;
import com.aica.aivoca.sentence.dto.SentenceRequestDto;
import com.aica.aivoca.sentence.dto.SentenceResponseDto;
import com.aica.aivoca.sentence.service.addSentenceService;
import com.aica.aivoca.sentence.service.getSentenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/sentence")
@RequiredArgsConstructor
public class SentenceController {

    private final addSentenceService addSentenceService;
    private final getSentenceService getSentenceService;

    @PostMapping
    public SuccessStatusResponse<SentenceResponseDto> addSentence(@RequestBody SentenceRequestDto requestDto) {
        return addSentenceService.addSentence(requestDto);
    }

    @GetMapping
    public SuccessStatusResponse<List<SentenceGetResponseDto>> getSentences(
            @RequestParam(value = "search", required = false) String search
    ) {
        return getSentenceService.getSentences(null, search); // userId는 무시됨
    }

}
