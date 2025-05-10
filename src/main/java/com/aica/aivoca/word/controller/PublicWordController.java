package com.aica.aivoca.word.controller;

import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.word.dto.WordGetResponseDto;
import com.aica.aivoca.word.service.PublicWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicWordController {

    private final PublicWordService publicWordService;

    @GetMapping("/word-lookup")
    public ResponseEntity<SuccessStatusResponse<WordGetResponseDto>> lookupWord(@RequestParam String word) {
        return ResponseEntity.ok(
                publicWordService.lookupAndSaveWordIfNeeded(word)
        );
    }
}
