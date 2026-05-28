package com.example.daily.domain.repository;

import com.example.daily.domain.entity.DiaryTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryTagRepository extends JpaRepository<DiaryTag, Long> {

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM diary_tags WHERE diary_id = :diaryId", nativeQuery = true)
    void deleteByDiaryId(@Param("diaryId") Long diaryId);
}