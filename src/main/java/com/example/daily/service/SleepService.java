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

        if (bedTime == null || wakeTime == null) {
            throw new RuntimeException("취침 시간과 기상 시간을 입력해주세요.");
        }
        if (bedTime.equals(wakeTime)) {
            throw new RuntimeException("취침 시간과 기상 시간이 같습니다.");
        }

        long minutes = Duration.between(bedTime, wakeTime).toMinutes();
        if (minutes < 0) {
            minutes += 24 * 60;
        }
        if (minutes > 24 * 60) {
            throw new RuntimeException("수면 시간은 24시간을 초과할 수 없습니다.");
        }
        if (minutes < 1) {
            throw new RuntimeException("수면 시간은 1분 이상이어야 합니다.");
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