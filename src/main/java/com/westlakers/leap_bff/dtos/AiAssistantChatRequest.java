package com.westlakers.leap_bff.dtos;

import jakarta.validation.constraints.Size;

public record AiAssistantChatRequest(
	@Size(max = 500) String question,
	String questionId) {
}