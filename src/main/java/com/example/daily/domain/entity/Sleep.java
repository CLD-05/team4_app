package com.example.daily.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sleeps",
        indexes = {
                @Index(name = "idx_sleeps_user_date", columnList = "user_id, diary_date")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_sleeps_user_date", columnNames = {"user_id", "diary_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sleep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "diary_date", nullable = false)
    private LocalDate diaryDate;

    @Column(name = "bed_time", nullable = false)
    private LocalDateTime bedTime;

    @Column(name = "wake_time", nullable = false)
    private LocalDateTime wakeTime;

    @Column(name = "total_minutes", nullable = false)
    private Integer totalMinutes;
}
