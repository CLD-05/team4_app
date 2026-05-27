package com.example.daily.service;

import com.example.daily.domain.entity.Diary;
import com.example.daily.domain.entity.DiaryTag;
import com.example.daily.domain.entity.User;
import com.example.daily.domain.repository.DiaryRepository;
import com.example.daily.domain.repository.UserRepository;
import com.example.daily.dto.DiaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.daily.domain.entity.Emotion;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createDiary(String email, DiaryDto.CreateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ↓ 추가 - targetDate 있으면 해당 날짜, 없으면 현재 시각
        LocalDateTime diaryDateTime = (request.getTargetDate() != null)
                ? request.getTargetDate().atStartOfDay()
                : LocalDateTime.now();

        Diary diary = Diary.builder()
                .user(user)
                .emotion(request.getEmotion())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .createdAt(diaryDateTime)
                .build();

        if (request.getTags() != null) {
            for (String tagName : request.getTags()) {
                DiaryTag tag = DiaryTag.builder()
                        .diary(diary)
                        .tagName(tagName)
                        .build();
                diary.getTags().add(tag);
            }
        }

        diaryRepository.save(diary);
    }

    @Transactional(readOnly = true)
    public List<DiaryDto> getEmotions(String email, int year, int month) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusNanos(1);

        return diaryRepository.findAllByUserAndCreatedAtBetween(user, start, end).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<DiaryDto> getTimeline(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return diaryRepository.findAllByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::convertToDto);
    }

    @Transactional
    public void deleteDiary(Long diaryId, String email) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("Diary not found"));

        if (!diary.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        diaryRepository.delete(diary);
    }

    @Transactional
    public void updateEmotion(String email, LocalDate date, Emotion emotion) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59, 999999999);

        Diary diary = diaryRepository.findByUserAndCreatedAtBetween(user, start, end)
                .stream()
                .findFirst()
                .orElse(Diary.builder()
                        .user(user)
                        .content("")
                        .createdAt(start)
                        .build());

        diary.setEmotion(emotion);
        diaryRepository.save(diary);


    }


    @Transactional(readOnly = true)
    public DiaryDto getDiaryByDate(String email, LocalDate date) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59, 999999999);

        return diaryRepository.findByUserAndCreatedAtBetween(user, start, end)
                .stream()
                .findFirst()
                .map(this::convertToDto)
                .orElse(null);
    }

    private DiaryDto convertToDto(Diary diary) {
        return DiaryDto.builder()
                .id(diary.getId())
                .emotion(diary.getEmotion())
                .content(diary.getContent())
                .imageUrl(diary.getImageUrl())
                .createdAt(diary.getCreatedAt())
                .tags(diary.getTags().stream().map(DiaryTag::getTagName).collect(Collectors.toList()))
                .build();
    }
    // 휴지통 조회
    @Transactional(readOnly = true)
    public List<DiaryDto> getDeletedDiaries(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return diaryRepository.findDeletedByUserId(user.getId())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 완전 삭제
    @Transactional
    public void hardDeleteDiary(Long diaryId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        diaryRepository.hardDeleteById(diaryId);
    }

    @Transactional
    public void restoreDiary(Long diaryId, String email) {
        diaryRepository.restoreById(diaryId);
    }


}
