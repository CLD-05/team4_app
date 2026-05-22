package com.example.daily.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diary_tags", indexes = {
    @Index(name = "idx_diary_tags_diary", columnList = "diary_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;
}
