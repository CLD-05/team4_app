package com.example.daily.domain.repository;

import com.example.daily.domain.entity.Diary;
import com.example.daily.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Page<Diary> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<Diary> findAllByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
    Optional<Diary> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
}
