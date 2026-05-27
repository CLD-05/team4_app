package com.example.daily.controller.api;

import com.example.daily.domain.entity.User;
import com.example.daily.domain.repository.SleepRepository;
import com.example.daily.domain.repository.UserRepository;
import com.example.daily.service.SleepService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/sleeps")
@RequiredArgsConstructor
public class SleepController {

    private final SleepService sleepService;
    private final SleepRepository sleepRepository;
    private final UserRepository userRepository;

    @Getter
    public static class SleepRequest {
        private LocalDate date;
        private LocalDateTime bedTime;
        private LocalDateTime wakeTime;
    }

    @Getter
    @Builder
    public static class SleepResponse {
        private LocalDate date;
        private String bedTime;
        private String wakeTime;
        private Integer totalMinutes;
    }

    @PostMapping
    public ResponseEntity<String> recordSleep(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestBody SleepRequest request) {
        sleepService.recordSleep(userDetails.getUsername(), request.getDate(), request.getBedTime(), request.getWakeTime());
        return ResponseEntity.ok("Sleep recorded successfully");
    }

    @GetMapping
    public ResponseEntity<SleepResponse> getSleep(@AuthenticationPrincipal UserDetails userDetails,
                                                  @RequestParam LocalDate date) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return sleepRepository.findByUserAndDiaryDate(user, date)
                .map(sleep -> ResponseEntity.ok(SleepResponse.builder()
                        .date(sleep.getDiaryDate())
                        .bedTime(sleep.getBedTime().toString())
                        .wakeTime(sleep.getWakeTime().toString())
                        .totalMinutes(sleep.getTotalMinutes())
                        .build()))
                .orElse(ResponseEntity.ok(null));
    }
}