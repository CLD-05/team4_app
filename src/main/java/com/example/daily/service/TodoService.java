package com.example.daily.service;

import com.example.daily.domain.entity.Todo;
import com.example.daily.domain.entity.User;
import com.example.daily.domain.repository.TodoRepository;
import com.example.daily.domain.repository.UserRepository;
import com.example.daily.dto.TodoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createTodo(String email, TodoDto.Request request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Todo todo = Todo.builder()
                .user(user)
                .todoDate(request.getTodoDate())
                .content(request.getContent())
                .build();

        todoRepository.save(todo);
    }

    @Transactional(readOnly = true)
    public List<TodoDto> getTodosByDate(String email, LocalDate date) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return todoRepository.findAllByUserAndTodoDate(user, date).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleTodo(Long todoId, String email) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        if (!todo.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        todo.setIsCompleted(!todo.getIsCompleted());
    }

    private TodoDto convertToDto(Todo todo) {
        return TodoDto.builder()
                .id(todo.getId())
                .todoDate(todo.getTodoDate())
                .content(todo.getContent())
                .isCompleted(todo.getIsCompleted())
                .build();
    }
}
