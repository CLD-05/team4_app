package com.example.daily.domain.repository;

import com.example.daily.domain.entity.Todo;
import com.example.daily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findAllByUserAndTodoDate(User user, LocalDate todoDate);
}
