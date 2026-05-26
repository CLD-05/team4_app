package com.example.daily.controller.api;

import com.example.daily.domain.entity.Exercise;
import com.example.daily.domain.entity.User;
import com.example.daily.domain.repository.ExerciseRepository;
import com.example.daily.domain.repository.UserRepository;
import com.example.daily.service.ExerciseService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<String> recordExercise(@AuthenticationPrincipal UserDetails userDetails,
                                                 @RequestBody ExerciseRequest request) {
        exerciseService.recordExercise(userDetails.getUsername(), request.getDate(), request.getCategory(), request.getMinutes());
        return ResponseEntity.ok("Exercise recorded successfully");
    }
    // 파일 하단이나 별도 DTO 클래스로 생성
    @Getter
    public static class ExerciseRequest {
        private LocalDate date;
        private String category;
        private int minutes;
    }

    @GetMapping
    public ResponseEntity<List<Exercise>> getExercises(@AuthenticationPrincipal UserDetails userDetails,
                                                       @RequestParam LocalDate date) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(exerciseRepository.findAllByUserAndDiaryDate(user, date));
    }
}

