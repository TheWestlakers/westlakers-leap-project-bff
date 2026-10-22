package com.westlakers.leap_bff.controllers;

import com.westlakers.leap_bff.dtos.AiAssistantChatRequest;
import com.westlakers.leap_bff.dtos.AiAssistantChatResponse;
import com.westlakers.leap_bff.services.AiAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-assistant")
public class AiAssistantController {
    private final AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    public AiAssistantChatResponse chat(@Valid @RequestBody AiAssistantChatRequest request) {
        return new AiAssistantChatResponse(aiAssistantService.answer(request.question(), request.questionId()));
    }
}