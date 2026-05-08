package com.example.issue15.profile.service;

import com.example.issue15.profile.dto.LoginRequest;
import com.example.issue15.profile.dto.RegisterRequest;
import com.example.issue15.profile.dto.UserProfileResponse;
import com.example.issue15.profile.entity.User;
import com.example.issue15.profile.exception.AuthenticationException;
import com.example.issue15.profile.exception.UserAlreadyExistsException;
import com.example.issue15.profile.exception.UserNotFoundException;
import com.example.issue15.profile.repository.UserRepository;
import com.example.issue15.profile.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户业务逻辑服务类
 * 处理用户注册、登录、信息管理等核心业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户注册
     * 创建新用户并返回用户信息
     *
     * @param registerRequest 注册请求DTO
     * @return 注册成功的用户信息
     * @throws UserAlreadyExistsException 如果用户名或邮箱已存在
     */
    @Transactional
    public UserProfileResponse register(RegisterRequest registerRequest) {
        log.info("开始用户注册流程，用户名: {}", registerRequest.getUsername());

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            log.warn("用户名已存在: {}", registerRequest.getUsername());
            throw new UserAlreadyExistsException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("邮箱已存在: {}", registerRequest.getEmail());
            throw new UserAlreadyExistsException("邮箱已存在");
        }

        // 创建新用户实体
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .nickname(registerRequest.getNickname())
                .avatar(registerRequest.getAvatar())
                .phone(registerRequest.getPhone())
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 保存用户到数据库
        User savedUser = userRepository.save(user);
        log.info("用户注册成功，用户ID: {}", savedUser.getId());

        return convertToUserProfileResponse(savedUser);
    }

    /**
     * 用户登录
     * 验证用户凭证并生成JWT Token
     *
     * @param loginRequest 登录请求DTO
     * @return 包含JWT Token的用户信息
     * @throws AuthenticationException 如果用户名或密码错误
     */
    @Transactional
    public UserProfileResponse login(LoginRequest loginRequest) {
        log.info("用户登录尝试，用户名: {}", loginRequest.getUsername());

        try {
            // 使用Spring Security进行身份验证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 获取认证后的用户信息
            User user = (User) authentication.getPrincipal();

            // 检查用户状态
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                log.warn("用户账户已被禁用: {}", loginRequest.getUsername());
                throw new AuthenticationException("账户已被禁用");
            }

            // 生成JWT Token
            String token = jwtTokenProvider.generateToken(user);
            log.info("用户登录成功，用户ID: {}", user.getId());

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);

            UserProfileResponse response = convertToUserProfileResponse(user);
            response.setToken(token);
            return response;

        } catch (org.springframework.security.core.AuthenticationException e) {
            log.error("用户登录失败，用户名: {}", loginRequest.getUsername());
            throw new AuthenticationException("用户名或密码错误");
        }
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     * @throws UserNotFoundException 如果用户不存在
     */
    public UserProfileResponse getUserById(Long userId) {
        log.info("查询用户信息，用户ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("用户不存在，用户ID: {}", userId);
                    return new UserNotFoundException("用户不存在");
                });

        return convertToUserProfileResponse(user);
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     * @throws UserNotFoundException 如果用户不存在
     */
    public UserProfileResponse getUserByUsername(String username) {
        log.info("查询用户信息，用户名: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("用户不存在，用户名: {}", username);
                    return new UserNotFoundException("用户不存在");
                });

        return convertToUserProfileResponse(user);
    }

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param updateRequest 更新请求DTO
     * @return 更新后的用户信息
     * @throws UserNotFoundException 如果用户不存在
     */
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UpdateProfileRequest updateRequest) {
        log.info("更新用户信息，用户ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("用户不存在，用户ID: {}", userId);
                    return new UserNotFoundException("用户不存在");
                });

        // 更新用户信息
        Optional.ofNullable(updateRequest.getNickname()).ifPresent(user::setNickname);
        Optional.ofNullable(updateRequest.getAvatar()).ifPresent(user::setAvatar);
        Optional.ofNullable(updateRequest.getPhone()).ifPresent(user::setPhone);
        Optional.ofNullable(updateRequest.getEmail()).ifPresent(email -> {
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new UserAlreadyExistsException("邮箱已被其他用户使用");
            }
            user.setEmail(email);
        });

        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        log.info("用户信息更新成功，用户ID: {}", userId);

        return convertToUserProfileResponse(updatedUser);
    }

    /**
     * 修改用户密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @throws UserNotFoundException 如果用户不存在
     * @throws AuthenticationException 如果旧密码错误
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改用户密码，用户ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("用户不存在，用户ID: {}", userId);
                    return new UserNotFoundException("用户不存在");
                });

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("旧密码验证失败，用户ID: {}", userId);
            throw new AuthenticationException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("密码修改成功，用户ID: {}", userId);
    }

    /**
     * 删除用户（软删除）
     *
     * @param userId 用户ID
     * @throws UserNotFoundException 如果用户不存在
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("删除用户，用户ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("用户不存在，用户ID: {}", userId);
                    return new UserNotFoundException("用户不存在");
                });

        // 软删除：将用户状态设置为DELETED
        user.setStatus(User.UserStatus.DELETED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("用户删除成功，用户ID: {}", userId);
    }

    /**
     * 将User实体转换为UserProfileResponse DTO
     *
     * @param user 用户实体
     * @return 用户信息DTO
     */
    private UserProfileResponse convertToUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }
}
