package com.example.daily.controller.api;

import com.example.daily.dto.UserDto;
import com.example.daily.service.UserService;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @PatchMapping("/profile-img")
    public ResponseEntity<String> updateProfileImg(@AuthenticationPrincipal UserDetails userDetails,
                                                   @RequestBody ProfileImgRequest request) {
        userService.updateProfileImg(userDetails.getUsername(), request.getProfileImg());
        return ResponseEntity.ok("Profile image updated");
    }

    @PatchMapping("/password")
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                                 @RequestBody PasswordChangeRequest request) {
        userService.changePassword(userDetails.getUsername(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok("Password changed successfully");
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.ok("Account deleted successfully");
    }

    @Getter
    @NoArgsConstructor
    static class PasswordChangeRequest {
        private String currentPassword;
        private String newPassword;
    }

    @Getter
    @NoArgsConstructor
    static class ProfileImgRequest {
        private String profileImg;
    }

    @GetMapping("/find-id")
    public ResponseEntity<String> findId(@RequestParam String email) {
        return ResponseEntity.ok(userService.findIdByEmail(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getLoginId(), request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok("비밀번호가 재설정되었습니다.");
    }

    @Getter
    @NoArgsConstructor
    static class ResetPasswordRequest {
        private String loginId;
        private String email;
        private String newPassword;
    }
}