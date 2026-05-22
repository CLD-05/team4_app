package com.example.daily.controller.api;

import com.example.daily.dto.UserDto;
import com.example.daily.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMyProfile(userDetails.getUsername()));
    }

    @PatchMapping("/profile")
    public ResponseEntity<String> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                @RequestParam String nickname,
                                                @RequestParam(required = false) String profileImg) {
        userService.updateProfile(userDetails.getUsername(), nickname, profileImg);
        return ResponseEntity.ok("Profile updated successful");
    }
}
