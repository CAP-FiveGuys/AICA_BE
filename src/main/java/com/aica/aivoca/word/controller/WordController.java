package com.aica.aivoca.word.controller;

import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.global.jwt.CustomUserDetails;
import com.aica.aivoca.word.dto.WordAddRequestDto;
import com.aica.aivoca.word.dto.WordResponseDto;
import com.aica.aivoca.word.dto.WordGetResponseDto;
import com.aica.aivoca.word.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    // ✅ 단어장에 단어 추가
    @PostMapping("/word/add")
    public ResponseEntity<SuccessStatusResponse<WordResponseDto>> addWordToVocabulary(
            @RequestBody WordAddRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                wordService.addWordToVocabulary(requestDto, userDetails.userId())
        );
    }

    // ✅ 단어장 전체 단어 조회
    @GetMapping("/word")
    public ResponseEntity<SuccessStatusResponse<List<WordGetResponseDto>>> getMyVocabularyWords(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                wordService.getMyVocabularyWords(userDetails.userId())
        );
    }
}