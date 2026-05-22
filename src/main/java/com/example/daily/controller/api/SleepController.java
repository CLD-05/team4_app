package com.example.daily.controller.api;

import com.example.daily.domain.entity.Sleep;
import com.example.daily.domain.repository.SleepRepository;
import com.example.daily.domain.entity.User;
import com.example.daily.domain.repository.UserRepository;
import com.example.daily.service.SleepService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

    @PostMapping
    public ResponseEntity<String> recordSleep(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestParam LocalDate date,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bedTime,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime wakeTime) {
        sleepService.recordSleep(userDetails.getUsername(), date, bedTime, wakeTime);
        return ResponseEntity.ok("Sleep recorded successfully");
    }

    @GetMapping
    public ResponseEntity<Sleep> getSleep(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestParam LocalDate date) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(sleepRepository.findByUserAndDiaryDate(user, date).orElse(null));
    }
}
