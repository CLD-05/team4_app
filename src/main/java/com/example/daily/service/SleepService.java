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
        // 자정을 넘긴 경우(음수) 방어 처리
        if (minutes < 0) {
            minutes += 24 * 60;
        }
        int totalMinutes = (int) minutes;

        Sleep sleep = sleepRepository.findByUserAndDiaryDate(user, date)
                .orElseGet(() -> Sleep.builder().user(user).diaryDate(date).build());

        sleep.setBedTime(bedTime);
        sleep.setWakeTime(wakeTime);
        sleep.setTotalMinutes(totalMinutes);

        sleepRepository.save(sleep);
    }
}
