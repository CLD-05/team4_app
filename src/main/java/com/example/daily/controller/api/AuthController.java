package com.example.daily.controller.api;

import com.example.daily.dto.UserDto;
import com.example.daily.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping(value = "/sign-up", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> signUp(
            @RequestPart("request") UserDto.SignUpRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        userService.signUp(request, file);
        return ResponseEntity.ok("Sign up successful");
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto.TokenResponse> login(@RequestBody UserDto.LoginRequest request,
                                                       HttpServletResponse response) {
        UserDto.TokenResponse tokens = userService.login(request);

        Cookie cookie = new Cookie("accessToken", tokens.getAccessToken());
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24);
        response.addCookie(cookie);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserDto.TokenResponse> refresh(@RequestBody String refreshToken) {
        return ResponseEntity.ok(userService.refresh(refreshToken));
    }
}