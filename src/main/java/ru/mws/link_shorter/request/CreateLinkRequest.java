package ru.mws.link_shorter.request;

import jakarta.validation.constraints.*;

public record CreateLinkRequest(
    @NotBlank(message = "URL не может быть пустым")
    @Size(max = 128, message = "Длина URL не должна превышать 128 символов")
    String url,

    @NotNull(message = "Длина не может быть null")
    @Min(value = 3, message = "Длина должна быть не менее 3 символов")
    @Max(value = 12, message = "Длина должна быть не более 12 символов")
    Integer len
) {}