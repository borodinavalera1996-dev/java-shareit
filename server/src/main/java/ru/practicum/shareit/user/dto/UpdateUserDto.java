package ru.practicum.shareit.user.dto;

import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class UpdateUserDto {
    @Nullable
    String name;

    @Nullable
    String email;
}
