package com.example.daily.domain.repository;

import com.example.daily.domain.entity.DiaryTag;
import org.springframework.data.jpa.repository.JpaRepository;

// ✅ 이거 하나만 만들어두면 Spring이 알아서 저장 기능을 다 만들어줍니다!
public interface DiaryTagRepository extends JpaRepository<DiaryTag, Long> {
}