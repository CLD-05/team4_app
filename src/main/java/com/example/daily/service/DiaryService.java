package com.example.daily.service;

import com.example.daily.domain.entity.Diary;
import com.example.daily.domain.repository.DiaryTagRepository;
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
    private final DiaryTagRepository diaryTagRepository;

    @Transactional
    public void createDiary(String email, DiaryDto.CreateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getTargetDate() != null && request.getTargetDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("미래 날짜에는 일기를 작성할 수 없습니다.");
        }

        LocalDateTime diaryDateTime = (request.getTargetDate() != null)
                ? request.getTargetDate().atStartOfDay()
                : LocalDateTime.now();

        LocalDateTime start = diaryDateTime.toLocalDate().atStartOfDay();
        LocalDateTime end   = diaryDateTime.toLocalDate().atTime(23, 59, 59, 999999999);

        Diary diary = diaryRepository.findByUserAndCreatedAtBetween(user, start, end)
                .stream().findFirst()
                .orElseGet(() -> Diary.builder()
                        .user(user)
                        .createdAt(diaryDateTime)
                        .build());

        diary.setEmotion(request.getEmotion());
        diary.setContent(request.getContent());
        diary.setImageUrl(request.getImageUrl());
        diaryRepository.saveAndFlush(diary);

        // 태그: DiaryTagRepository로만 직접 관리 (컬렉션 건드리지 않음)
        diaryTagRepository.deleteByDiaryId(diary.getId());
        if (request.getTags() != null) {
            if (request.getTags().size() > 10) {
                throw new RuntimeException("태그는 최대 10개까지 추가할 수 있습니다.");
            }
            for (String tagName : request.getTags()) {
                if (tagName.isBlank()) continue;
                if (tagName.length() > 20) {
                    throw new RuntimeException("태그는 20자 이하여야 합니다.");
                }
                diaryTagRepository.save(DiaryTag.builder()
                        .diary(diary)
                        .tagName(tagName)
                        .build());
            }
        }
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
        Diary diary = diaryRepository.findDeletedById(diaryId)
                .orElseThrow(() -> new RuntimeException("일기를 찾을 수 없습니다."));
        if (!diary.getUser().getEmail().equals(email)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
        diaryRepository.hardDeleteById(diaryId);
    }

    @Transactional
    public void restoreDiary(Long diaryId, String email) {
        Diary diary = diaryRepository.findDeletedById(diaryId)
                .orElseThrow(() -> new RuntimeException("일기를 찾을 수 없습니다."));
        if (!diary.getUser().getEmail().equals(email)) {
            throw new RuntimeException("복구 권한이 없습니다.");
        }
        diaryRepository.restoreById(diaryId);
    }


}