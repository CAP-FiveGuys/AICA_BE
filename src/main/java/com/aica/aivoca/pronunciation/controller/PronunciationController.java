package com.aica.aivoca.pronunciation.controller;

import com.aica.aivoca.pronunciation.service.PronunciationAssessmentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/pronunciation")
public class PronunciationController {

    private final PronunciationAssessmentService pronunciationService;

    public PronunciationController(PronunciationAssessmentService pronunciationService) {
        this.pronunciationService = pronunciationService;
    }

}