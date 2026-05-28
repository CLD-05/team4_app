package com.example.daily.service;

import com.example.daily.domain.entity.Emotion;
import com.example.daily.domain.entity.Diary;
import com.example.daily.domain.entity.DiaryTag;
import com.example.daily.domain.entity.User;
import com.example.daily.domain.repository.DiaryRepository;
import com.example.daily.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyReport(String email, int year, int month) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusNanos(1);

        List<Diary> diaries = diaryRepository.findAllByUserAndCreatedAtBetween(user, start, end);

        Map<Emotion, Long> emotionCounts = diaries.stream()
                .collect(Collectors.groupingBy(Diary::getEmotion, Collectors.counting()));

        List<Map<String, String>> goodTags = getTopTagsWithDate(diaries, Arrays.asList(Emotion.VERY_GOOD, Emotion.GOOD));
        List<Map<String, String>> badTags  = getTopTagsWithDate(diaries, Arrays.asList(Emotion.VERY_BAD, Emotion.BAD));

        Map<String, Object> report = new HashMap<>();
        report.put("emotionCounts", emotionCounts);
        report.put("goodTags", goodTags);
        report.put("badTags", badTags);
        report.put("totalRecords", diaries.size());

        return report;
    }

    private List<Map<String, String>> getTopTagsWithDate(List<Diary> diaries, List<Emotion> targetEmotions) {
        Map<String, Long>   tagCounts = new LinkedHashMap<>();
        Map<String, String> tagDates  = new LinkedHashMap<>();

        diaries.stream()
                .filter(d -> targetEmotions.contains(d.getEmotion()))
                .sorted(Comparator.comparing(Diary::getCreatedAt).reversed()) // 최신순
                .forEach(d -> {
                    String dateStr = d.getCreatedAt().toLocalDate().toString();
                    d.getTags().forEach(tag -> {
                        String name = tag.getTagName();
                        tagCounts.merge(name, 1L, Long::sum);
                        tagDates.put(name, dateStr); // 최신 날짜로 덮어쓰기
                    });
                });

        return tagCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(e -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("tag",  e.getKey());
                    m.put("date", tagDates.get(e.getKey()));
                    return m;
                })
                .collect(Collectors.toList());
    }
}