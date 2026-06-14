package ru.practicum.shareit.user.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class UpdateUserDto {
    @Nullable
    String name;

    @Nullable
    @Email(message = "Некорректный формат email")
    String email;
}
