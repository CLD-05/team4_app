package com.example.daily.service;

import com.example.daily.domain.entity.User;
import com.example.daily.domain.repository.*;
import com.example.daily.dto.UserDto;
import com.example.daily.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

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
    private final S3Service s3Service; // 👈 추가

    // 기존 signUp 메서드는 유지하되 내부에서 이미지를 null로 처리하거나 아래 오버로딩 메서드로 위임
    @Transactional
    public void signUp(UserDto.SignUpRequest request) {
        try {
            signUp(request, null);
        } catch (IOException e) {
            throw new RuntimeException("회원가입 중 파일 업로드 실패", e);
        }
    }

    // 👈 회원가입 시 이미지 동시 업로드를 위한 신규 메서드 추가
    @Transactional
    public void signUp(UserDto.SignUpRequest request, MultipartFile file) throws IOException {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        if (request.getLoginId() != null && userRepository.existsByLoginId(request.getLoginId())) {
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
        }

        // S3에 이미지 업로드 후 URL 확보
        String profileImgUrl = null;
        if (file != null && !file.isEmpty()) {
            profileImgUrl = s3Service.upload(file);
        }
        User user = User.builder()
                .email(request.getEmail())
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileImg(profileImgUrl) // 👈 S3 URL 주소가 바로 저장됨
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

    // 👈 기존 로컬 복사 코드 걷어내고 S3 연동으로 전면 수정
    public String uploadProfileImg(String email, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        // S3에 업로드하고 가져온 완전한 URL 주소
        String url = s3Service.upload(file); 
        updateProfileImg(email, url);
        return url;
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
}