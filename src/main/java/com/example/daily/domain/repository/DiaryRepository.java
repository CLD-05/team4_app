package com.example.daily.domain.repository;

import com.example.daily.domain.entity.Diary;
import com.example.daily.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    void deleteByUser(User user);
    List<Diary> findAllByUser(User user);

    @Modifying
    @Query(value = "DELETE FROM diary_tags WHERE diary_id IN (SELECT id FROM diaries WHERE user_id = :userId)", nativeQuery = true)
    void deleteAllTagsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM diaries WHERE user_id = :userId", nativeQuery = true)
    void deleteAllByUserId(@Param("userId") Long userId);

    Page<Diary> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<Diary> findAllByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
    List<Diary> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT * FROM diaries WHERE user_id = :userId AND deleted_at IS NOT NULL ORDER BY deleted_at DESC", nativeQuery = true)
    List<Diary> findDeletedByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM diaries WHERE id = :id AND deleted_at IS NOT NULL LIMIT 1", nativeQuery = true)
    Optional<Diary> findDeletedById(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM diaries WHERE id = :id AND deleted_at IS NOT NULL", nativeQuery = true)
    void hardDeleteById(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE diaries SET deleted_at = NULL WHERE id = :id", nativeQuery = true)
    void restoreById(@Param("id") Long id);

    // ✅ 계정 삭제 시 모든 일기 이미지 URL 조회 (Soft Delete 포함)
    @Query(value = "SELECT image_url FROM diaries WHERE user_id = :userId AND image_url IS NOT NULL", nativeQuery = true)
    List<String> findAllImageUrlsByUserId(@Param("userId") Long userId);
}