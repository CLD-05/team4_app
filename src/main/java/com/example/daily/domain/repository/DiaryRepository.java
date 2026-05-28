package com.example.daily.domain.repository;

import com.example.daily.domain.entity.Diary;
import com.example.daily.domain.entity.User;
import com.example.daily.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    void deleteByUser(User user);
    List<Diary> findAllByUser(User user);
    Page<Diary> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<Diary> findAllByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
    List<Diary> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end); // Optional → List
    // 삭제된 일기 조회 (SQLRestriction 우회 - 네이티브 쿼리 사용)
    @Query(value = "SELECT * FROM diaries WHERE user_id = :userId AND deleted_at IS NOT NULL ORDER BY deleted_at DESC", nativeQuery = true)
    List<Diary> findDeletedByUserId(@Param("userId") Long userId);

    // 완전 삭제
    @Modifying
    @Query(value = "DELETE FROM diaries WHERE id = :id AND deleted_at IS NOT NULL", nativeQuery = true)
    void hardDeleteById(@Param("id") Long id);


    @Modifying
    @Query(value = "UPDATE diaries SET deleted_at = NULL WHERE id = :id", nativeQuery = true)
    void restoreById(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM diary_tags WHERE diary_id IN (SELECT id FROM diaries WHERE user_id = :userId)", nativeQuery = true)
    void deleteAllTagsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM diaries WHERE user_id = :userId", nativeQuery = true)
    void deleteAllByUserId(@Param("userId") Long userId);

}