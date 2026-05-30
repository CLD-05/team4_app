package com.example.daily.controller.api;

import com.example.daily.dto.DiaryDto;
import com.example.daily.service.DiaryService;
import com.example.daily.service.S3Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.example.daily.domain.entity.Emotion;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final S3Service s3Service;

    @PostMapping
    public ResponseEntity<String> createDiary(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DiaryDto.CreateRequest request) {
        diaryService.createDiary(userDetails.getUsername(), request);
        return ResponseEntity.ok("Diary created successfully");
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = s3Service.upload(file);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/emotions")
    public ResponseEntity<List<DiaryDto>> getEmotions(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(diaryService.getEmotions(userDetails.getUsername(), year, month));
    }

    @GetMapping("/timeline")
    public ResponseEntity<Page<DiaryDto>> getTimeline(@AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(diaryService.getTimeline(userDetails.getUsername(), pageable));
    }

    @DeleteMapping("/{diaryId}")
    public ResponseEntity<String> deleteDiary(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId, userDetails.getUsername());
        return ResponseEntity.ok("Diary deleted successfully");
    }

    @PatchMapping("/emotion")
    public ResponseEntity<String> updateEmotion(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam LocalDate date,
            @RequestParam Emotion emotion) {
        diaryService.updateEmotion(userDetails.getUsername(), date, emotion);
        return ResponseEntity.ok("Emotion updated successfully");
    }

    @GetMapping("/by-date")
    public ResponseEntity<DiaryDto> getDiaryByDate(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(diaryService.getDiaryByDate(userDetails.getUsername(), date));
    }

    @GetMapping("/trash")
    public ResponseEntity<List<DiaryDto>> getTrash(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(diaryService.getDeletedDiaries(userDetails.getUsername()));
    }

    @DeleteMapping("/{diaryId}/hard")
    public ResponseEntity<String> hardDelete(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long diaryId) {
        diaryService.hardDeleteDiary(diaryId, userDetails.getUsername());
        return ResponseEntity.ok("Diary permanently deleted");
    }

    @PatchMapping("/{diaryId}/restore")
    public ResponseEntity<String> restore(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long diaryId) {
        diaryService.restoreDiary(diaryId, userDetails.getUsername());
        return ResponseEntity.ok("Diary restored successfully");
    }
}