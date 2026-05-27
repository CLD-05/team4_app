package com.example.daily.controller.api;

import com.example.daily.dto.TodoDto;
import com.example.daily.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    public ResponseEntity<String> createTodo(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestBody TodoDto.Request request) {
        todoService.createTodo(userDetails.getUsername(), request);
        return ResponseEntity.ok("Todo created successfully");
    }

    @GetMapping
    public ResponseEntity<List<TodoDto>> getTodos(@AuthenticationPrincipal UserDetails userDetails,
                                                  @RequestParam LocalDate date) {
        return ResponseEntity.ok(todoService.getTodosByDate(userDetails.getUsername(), date));
    }

    @PatchMapping("/{todoId}/toggle")
    public ResponseEntity<String> toggleTodo(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Long todoId) {
        todoService.toggleTodo(todoId, userDetails.getUsername());
        return ResponseEntity.ok("Todo toggled successfully");
    }

    @DeleteMapping("/{todoId}")
    public ResponseEntity<String> deleteTodo(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Long todoId) {
        todoService.deleteTodo(todoId, userDetails.getUsername());
        return ResponseEntity.ok("Todo deleted successfully");
    }
}
