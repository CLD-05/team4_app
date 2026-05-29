package com.example.daily.service;

import com.example.daily.domain.entity.User;
import com.example.daily.domain.repository.*;
import com.example.daily.dto.UserDto;
import com.example.daily.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final DiaryRepository diaryRepository;
    private final SleepRepository sleepRepository;
    private final ExerciseRepository exerciseRepository;
    private final TodoRepository todoRepository;

    @Transactional
    public void signUp(UserDto.SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        if (request.getLoginId() != null && userRepository.existsByLoginId(request.getLoginId())) {
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
        }
        User user = User.builder()
                .email(request.getEmail())
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();
        userRepository.save(user);
    }

    @Transactional
    public UserDto.TokenResponse login(UserDto.LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseGet(() -> userRepository.findByEmail(request.getLoginId())
                        .orElseThrow(() -> new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다.")));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        String accessToken  = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
        user.setRefreshToken(refreshToken);
        return UserDto.TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public UserDto.TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String email = jwtTokenProvider.getEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new RuntimeException("Token mismatch");
        }
        String accessToken = jwtTokenProvider.createAccessToken(email);
        return UserDto.TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional(readOnly = true)
    public UserDto getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImg(user.getProfileImg())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public void updateProfile(String email, String nickname, String profileImg) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setNickname(nickname);
        if (profileImg != null) user.setProfileImg(profileImg);
    }

    @Transactional
    public void updateProfileImg(String email, String profileImg) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setProfileImg(profileImg);
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 올바르지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        todoRepository.deleteByUser(user);
        sleepRepository.deleteByUser(user);
        exerciseRepository.deleteByUser(user);
        diaryRepository.deleteAllTagsByUserId(user.getId());
        diaryRepository.deleteAllByUserId(user.getId());
        userRepository.delete(user);
    }

    public String findIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일로 가입된 계정이 없습니다."));
        return user.getLoginId() != null ? user.getLoginId() : user.getEmail();
    }

    @Transactional
    public void resetPassword(String loginId, String email, String newPassword) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("해당 아이디로 가입된 계정이 없습니다."));
        if (!user.getEmail().equals(email)) {
            throw new RuntimeException("아이디와 이메일이 일치하지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
    }
}