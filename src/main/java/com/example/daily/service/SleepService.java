package com.example.daily.service;

import com.example.daily.domain.entity.Sleep;
import com.example.daily.domain.entity.User;
import com.example.daily.domain.repository.SleepRepository;
import com.example.daily.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SleepService {

    private final SleepRepository sleepRepository;
    private final UserRepository userRepository;

    @Transactional
    public void recordSleep(String email, LocalDate date, LocalDateTime bedTime, LocalDateTime wakeTime) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long minutes = Duration.between(bedTime, wakeTime).toMinutes();
        // 10분 단위 자동 계산 (반올림 또는 버림 선택 가능, 여기서는 가장 가까운 10분으로 반올림)
        int totalMinutes = (int) (Math.round(minutes / 10.0) * 10);

        Sleep sleep = sleepRepository.findByUserAndDiaryDate(user, date)
                .orElse(Sleep.builder().user(user).diaryDate(date).build());

        sleep.setBedTime(bedTime);
        sleep.setWakeTime(wakeTime);
        sleep.setTotalMinutes(totalMinutes);

        sleepRepository.save(sleep);
    }
}
