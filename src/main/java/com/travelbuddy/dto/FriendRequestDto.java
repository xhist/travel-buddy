package com.travelbuddy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDto {
    @NotNull
    private Long id;
    @NotNull
    private UserDto sender;
}
