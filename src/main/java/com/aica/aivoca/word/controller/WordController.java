package com.aica.aivoca.word.controller;

import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.global.jwt.CustomUserDetails;
import com.aica.aivoca.word.dto.WordAddRequestDto;
import com.aica.aivoca.word.dto.WordResponseDto;
import com.aica.aivoca.word.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/word")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @PostMapping("/add")
    public ResponseEntity<SuccessStatusResponse<WordResponseDto>> addWordToVocabulary(
            @RequestBody WordAddRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                wordService.addWordToVocabulary(requestDto, userDetails.userId())
        );
    }
}