package com.gameexpert.player.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreatePlayerRequest {
    // Lv 3: 2~12글자의 영문 대소문자, 숫자와 밑줄을 허용하는 검증
    @NotNull
    @Size(min = 2, max = 12)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private final String nickname;
}
