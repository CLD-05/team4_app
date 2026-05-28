package com.example.daily.service;

import com.example.daily.domain.entity.User;
import com.example.daily.domain.entity.Diary;
import com.example.daily.domain.repository.UserRepository;
import com.example.daily.domain.repository.DiaryRepository;
import com.example.daily.domain.repository.DiaryTagRepository;
import com.example.daily.domain.repository.SleepRepository;
import com.example.daily.domain.repository.ExerciseRepository;
import com.example.daily.domain.repository.TodoRepository;
import java.util.List;
import com.example.daily.dto.UserDto;
import com.example.daily.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime; // 추가
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final DiaryRepository diaryRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final SleepRepository sleepRepository;
    private final ExerciseRepository exerciseRepository;
    private final TodoRepository todoRepository;

    @Transactional
    public void signUp(UserDto.SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())

                .build();

        userRepository.save(user);
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
                .createdAt(user.getCreatedAt()) // ← 추가
                .build();
    }

    @Transactional
    public UserDto.TokenResponse login(UserDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String accessToken = jwtTokenProvider.createAccessToken(request.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRefreshToken(refreshToken);

        return UserDto.TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void updateProfile(String email, String nickname, String profileImg) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setNickname(nickname);
        if (profileImg != null) {
            user.setProfileImg(profileImg);
        }
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
    public void updateProfileImg(String email, String profileImg) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setProfileImg(profileImg);
    }

    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        todoRepository.deleteByUser(user);
        sleepRepository.deleteByUser(user);
        exerciseRepository.deleteByUser(user);
        diaryRepository.deleteAllTagsByUserId(user.getId());  // tags 먼저
        diaryRepository.deleteAllByUserId(user.getId());       // diaries
        userRepository.delete(user);
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
}