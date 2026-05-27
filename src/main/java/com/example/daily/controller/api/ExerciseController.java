package com.example.daily.controller.api;

import com.example.daily.domain.entity.User;
import com.example.daily.domain.repository.ExerciseRepository;
import com.example.daily.domain.repository.UserRepository;
import com.example.daily.service.ExerciseService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    @Getter
    public static class ExerciseRequest {
        private LocalDate date;
        private String category;
        private int minutes;
    }

    @Getter
    @Builder
    public static class ExerciseResponse {
        private LocalDate date;
        private String category;
        private Integer durationMinutes;
    }

    @PostMapping
    public ResponseEntity<String> recordExercise(@AuthenticationPrincipal UserDetails userDetails,
                                                 @RequestBody ExerciseRequest request) {
        exerciseService.recordExercise(userDetails.getUsername(), request.getDate(), request.getCategory(), request.getMinutes());
        return ResponseEntity.ok("Exercise recorded successfully");
    }

    @GetMapping
    public ResponseEntity<List<ExerciseResponse>> getExercises(@AuthenticationPrincipal UserDetails userDetails,
                                                               @RequestParam LocalDate date) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ExerciseResponse> result = exerciseRepository.findAllByUserAndDiaryDate(user, date)
                .stream()
                .map(e -> ExerciseResponse.builder()
                        .date(e.getDiaryDate())
                        .category(e.getCategory())
                        .durationMinutes(e.getDurationMinutes())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}