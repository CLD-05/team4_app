package com.example.daily.domain.repository;

import com.example.daily.domain.entity.Exercise;
import com.example.daily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findAllByUserAndDiaryDate(User user, LocalDate diaryDate);
}
