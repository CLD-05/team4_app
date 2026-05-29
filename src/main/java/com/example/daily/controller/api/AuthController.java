package com.example.daily.controller.api;

import com.example.daily.dto.UserDto;
import com.example.daily.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@Valid @RequestBody UserDto.SignUpRequest request) {
        userService.signUp(request);
        return ResponseEntity.ok("Sign up successful");
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto.TokenResponse> login(@RequestBody UserDto.LoginRequest request,
                                                       HttpServletResponse response) {
        UserDto.TokenResponse tokens = userService.login(request);

        Cookie cookie = new Cookie("accessToken", tokens.getAccessToken());
        cookie.setHttpOnly(false); // JS에서 읽지 않으므로 true도 가능
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 24시간
        response.addCookie(cookie);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserDto.TokenResponse> refresh(@RequestBody String refreshToken) {
        return ResponseEntity.ok(userService.refresh(refreshToken));
    }
}