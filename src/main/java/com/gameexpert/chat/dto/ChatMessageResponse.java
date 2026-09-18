package com.gameexpert.chat.dto;

import java.time.LocalDateTime;

import com.gameexpert.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {

    private final String sender;
    private final String content;
    private final LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.getSenderNickname(),
                chatMessage.getContent(),
                chatMessage.getCreatedAt()
        );
    }
}
