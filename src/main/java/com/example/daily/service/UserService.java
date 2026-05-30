package com.example.daily.service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
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
    private final S3Service s3Service;


    @Transactional
    public void signUp(UserDto.SignUpRequest request) {
        try {
            signUp(request, null);
        } catch (IOException e) {
            throw new RuntimeException("회원가입 중 파일 업로드 실패", e);
        }
    }

    @Transactional
    public void signUp(UserDto.SignUpRequest request, MultipartFile file) throws IOException {
        if (request.getEmail() == null || !request.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new RuntimeException("올바른 이메일 형식이 아닙니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        if (request.getLoginId() == null || request.getLoginId().length() < 3 || request.getLoginId().length() > 20) {
            throw new RuntimeException("아이디는 3자 이상 20자 이하여야 합니다.");
        }
        if (request.getLoginId() != null && userRepository.existsByLoginId(request.getLoginId())) {
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
        }
        if (request.getNickname() == null || request.getNickname().isBlank()) {
            throw new RuntimeException("닉네임을 입력해주세요.");
        }
        if (request.getNickname().length() > 20) {
            throw new RuntimeException("닉네임은 20자 이하여야 합니다.");
        }
        String password = request.getPassword();
        if (password == null || password.length() < 8) {
            throw new RuntimeException("비밀번호는 8자 이상이어야 합니다.");
        }

        String profileImgUrl = null;
        if (file != null && !file.isEmpty()) {
            profileImgUrl = s3Service.upload(file);
        }

        User user = User.builder()
                .email(request.getEmail())
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileImg(profileImgUrl)
                .build();
        userRepository.save(user);
    }

    @Transactional
    public UserDto.TokenResponse login(UserDto.LoginRequest request) {
        // loginId로 찾고, JWT엔 email 저장 (기존 코드 호환)
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

    public String uploadProfileImg(String email, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
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
        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("비밀번호는 8자 이상이어야 합니다.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void deleteAccount(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }
        todoRepository.deleteByUser(user);
        sleepRepository.deleteByUser(user);
        exerciseRepository.deleteByUser(user);
        diaryRepository.deleteAllTagsByUserId(user.getId());
        diaryRepository.deleteAllByUserId(user.getId());
        userRepository.delete(user);
    }
}