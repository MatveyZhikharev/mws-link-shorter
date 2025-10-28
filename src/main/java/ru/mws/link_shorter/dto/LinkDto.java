package ru.mws.link_shorter.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация о сокращенной ссылке")
public record LinkDto(
    @Schema(description = "Короткий ключ", example = "AbCdEf")
    String shortKey,

    @Schema(description = "Оригинальный URL", example = "https://example.com/very/long/url")
    String originalUrl,

    @Schema(description = "Количество переходов", example = "42")
    int clickCount
) {}