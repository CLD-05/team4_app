package com.example.daily.dto;

import com.example.daily.domain.entity.Emotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryDto {
    private Long id;
    private Emotion emotion;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private List<String> tags;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Emotion emotion;
        private String content;
        private String imageUrl;
        private List<String> tags;
        private LocalDate targetDate;
    }
}