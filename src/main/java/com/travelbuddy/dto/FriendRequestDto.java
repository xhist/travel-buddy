package com.travelbuddy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FriendRequestDto {
    @NotNull
    private Long senderId;
    @NotNull
    private Long receiverId;
}
