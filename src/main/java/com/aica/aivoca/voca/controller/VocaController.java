package com.aica.aivoca.voca.controller;

import com.aica.aivoca.voca.dto.ErrorResponseDto;
import com.aica.aivoca.voca.service.ExampleGenerationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.aica.aivoca.voca.dto.ExampleDto;
import com.aica.aivoca.voca.dto.SuccessResponseDto;

@RestController
@RequestMapping("/api/voca")
public class VocaController {

    private final ExampleGenerationService exampleGenerationService;

    public VocaController(ExampleGenerationService exampleGenerationService) {
        this.exampleGenerationService = exampleGenerationService;
    }

    @GetMapping
    public ResponseEntity<?> getExample(@RequestParam(required = true) String word) {
        if (word == null || word.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponseDto(400, "단어는 필수 입력값입니다."));
        }

        try {
            String example = exampleGenerationService.generateExample(word);
            return ResponseEntity.ok(new SuccessResponseDto<>(200, "예문 생성 성공", new ExampleDto(word, example)));
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(new ErrorResponseDto(500, "예문 생성에 실패했습니다. 다시 시도해 주세요."));
        }
    }



    // 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception e) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Example generation failed: " + e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
