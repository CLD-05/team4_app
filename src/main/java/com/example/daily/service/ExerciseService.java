package com.example.daily.service;

import com.example.daily.domain.entity.Exercise;
import com.example.daily.domain.entity.User;
import com.example.daily.domain.repository.ExerciseRepository;
import com.example.daily.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    @Transactional
    public void recordExercise(String email, LocalDate date, String category, int minutes) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 10분 단위 기록 (요구사항: 10분 단위로 기록)
        int durationMinutes = (int) (Math.round(minutes / 10.0) * 10);

        Exercise exercise = Exercise.builder()
                .user(user)
                .diaryDate(date)
                .category(category)
                .durationMinutes(durationMinutes)
                .build();

        exerciseRepository.save(exercise);
    }
}
