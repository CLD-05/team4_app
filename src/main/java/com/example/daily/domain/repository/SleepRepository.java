package com.example.daily.domain.repository;

import com.example.daily.domain.entity.Sleep;
import com.example.daily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SleepRepository extends JpaRepository<Sleep, Long> {
    Optional<Sleep> findByUserAndDiaryDate(User user, LocalDate diaryDate);
    void deleteByUser(User user);

}
