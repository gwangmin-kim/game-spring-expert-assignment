package com.gameexpert.ws.dto;

import java.time.LocalDateTime;

import com.gameexpert.chat.dto.ChatMessageResponse;
import lombok.Getter;

@Getter
public class ChatResponse {
    // Lv 13: API 명세에 맞게 응답 필드와 생성자를 완성합니다.
    private final String type = "chat";
    private final String sender;
    private final String content;
    private final LocalDateTime timestamp;

    public ChatResponse(String sender, String content, LocalDateTime timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public static ChatResponse from(ChatMessageResponse chatMessageResponse) {
        return new ChatResponse(
            chatMessageResponse.getSender(),
            chatMessageResponse.getContent(),
            chatMessageResponse.getCreatedAt()
        );
    }
}
