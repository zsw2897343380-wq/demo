package com.example.issue15.user.service;

import com.example.issue15.user.dto.LoginRequest;
import com.example.issue15.user.dto.RegisterRequest;
import com.example.issue15.user.dto.UserResponse;
import com.example.issue15.user.entity.User;
import com.example.issue15.user.exception.AuthenticationException;
import com.example.issue15.user.exception.UserAlreadyExistsException;
import com.example.issue15.user.exception.UserNotFoundException;
import com.example.issue15.user.repository.UserRepository;
import com.example.issue15.user.security.JwtTokenProvider;
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
 * 用户服务层 - 处理用户相关的业务逻辑
 * 包括用户注册、登录、信息管理等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求DTO
     * @return 注册成功的用户信息
     * @throws UserAlreadyExistsException 如果用户名或邮箱已存在
     */
    @Transactional
    public UserResponse register(RegisterRequest registerRequest) {
        log.info("开始用户注册流程，用户名: {}", registerRequest.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            log.warn("用户名已存在: {}", registerRequest.getUsername());
            throw new UserAlreadyExistsException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("邮箱已存在: {}", registerRequest.getEmail());
            throw new UserAlreadyExistsException("邮箱已被注册");
        }
        
        // 创建新用户实体
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .nickname(registerRequest.getNickname())
                .phone(registerRequest.getPhone())
                .avatar(registerRequest.getAvatar())
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // 保存用户到数据库
        User savedUser = userRepository.save(user);
        log.info("用户注册成功，用户ID: {}", savedUser.getId());
        
        // 生成JWT Token
        String token = jwtTokenProvider.generateToken(savedUser);
        
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .phone(savedUser.getPhone())
                .avatar(savedUser.getAvatar())
                .token(token)
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求DTO
     * @return 登录成功的用户信息（包含JWT Token）
     * @throws AuthenticationException 如果用户名或密码错误
     */
    @Transactional
    public UserResponse login(LoginRequest loginRequest) {
        log.info("用户登录尝试，用户名: {}", loginRequest.getUsername());
        
        try {
            // 使用Spring Security进行身份验证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            
            // 获取认证用户信息
            User user = (User) authentication.getPrincipal();
            
            // 检查用户状态
            if (user.getStatus() != UserStatus.ACTIVE) {
                log.warn("用户账号已被禁用: {}", loginRequest.getUsername());
                throw new AuthenticationException("账号已被禁用");
            }
            
            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);
            
            // 生成JWT Token
            String token = jwtTokenProvider.generateToken(user);
            
            log.info("用户登录成功，用户名: {}", loginRequest.getUsername());
            
            return UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .phone(user.getPhone())
                    .avatar(user.getAvatar())
                    .token(token)
                    .lastLoginTime(user.getLastLoginTime())
                    .build();
                    
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
    public UserResponse getUserById(Long userId) {
        log.debug("查询用户信息，用户ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("用户不存在，用户ID: {}", userId);
                    return new UserNotFoundException("用户不存在");
                });
        
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }

    /**
     * 根据用户名获取用户信息
     * 
     * @param username 用户名
     * @return 用户信息
     * @throws UserNotFoundException 如果用户不存在
     */
    public UserResponse getUserByUsername(String username) {
        log.debug("查询用户信息，用户名: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("用户不存在，用户名: {}", username);
                    return new UserNotFoundException("用户不存在");
                });
        
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .lastLoginTime(user.getLastLoginTime())
                .build();
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
    public UserResponse updateUser(Long userId, UpdateUserRequest updateRequest) {
        log.info("更新用户信息，用户ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("用户不存在，用户ID: {}", userId);
                    return new UserNotFoundException("用户不存在");
                });
        
        // 更新用户信息
        Optional.ofNullable(updateRequest.getNickname()).ifPresent(user::setNickname);
        Optional.ofNullable(updateRequest.getPhone()).ifPresent(user::setPhone);
        Optional.ofNullable(updateRequest.getAvatar()).ifPresent(user::setAvatar);
        Optional.ofNullable(updateRequest.getEmail()).ifPresent(email -> {
            // 检查新邮箱���否已被其他用户使用
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new UserAlreadyExistsException("邮箱已被其他用户使用");
            }
            user.setEmail(email);
        });
        
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        log.info("用户信息更新成功，用户ID: {}", userId);
        
        return UserResponse.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .nickname(updatedUser.getNickname())
                .phone(updatedUser.getPhone())
                .avatar(updatedUser.getAvatar())
                .status(updatedUser.getStatus())
                .createdAt(updatedUser.getCreatedAt())
                .updatedAt(updatedUser.getUpdatedAt())
                .build();
    }

    /**
     * 修改用户密码
     * 
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @throws AuthenticationException 如果旧密码错误
     * @throws UserNotFoundException 如果用户不存在
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改用户密码，用户ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("用户不存在，用户ID: {}", userId);
                    return new UserNotFoundException("用户不存在");
                });
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("旧密码错误，用户ID: {}", userId);
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
                    log.warn("用户不存在，用户ID: {}", userId);
                    return new UserNotFoundException("用户不存在");
                });
        
        // 软删除：将用户状态设置为禁用
        user.setStatus(UserStatus.DISABLED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("用户删除成功，用户ID: {}", userId);
    }

    /**
     * 验证JWT Token是否有效
     * 
     * @param token JWT Token
     * @return 如果Token有效返回true，否则返回false
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * 从JWT Token中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }
}
