package com.aica.aivoca.wordinfo.controller;

import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.word.dto.WordGetResponseDto;
import com.aica.aivoca.wordinfo.service.WordinfoSerpBridgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wordinfo")
@RequiredArgsConstructor
public class WordinfoController {

    private final WordinfoSerpBridgeService wordinfoSerpBridgeService;

    @GetMapping
    public ResponseEntity<SuccessStatusResponse<List<WordGetResponseDto>>> lookupWord(
            @RequestParam String word
    ) {
        return ResponseEntity.ok(
                // ✅ 무조건 “SERP 먼저 → 비면 GPT로 채우기 → 마지막에 기존 서비스로 다시 조회” 파이프라인
                wordinfoSerpBridgeService.lookupAndSaveWordIfNeededUsingSerpFirst(word)
        );
    }
}
