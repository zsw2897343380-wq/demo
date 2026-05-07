package com.example.usermanagement.user.service;

import com.example.usermanagement.common.dto.UserDto;
import com.example.usermanagement.common.exception.BusinessException;
import com.example.usermanagement.user.entity.UserEntity;
import com.example.usermanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务类，封装与用户实体相关的业务逻辑。
 * 包括用户信息的查询、更新和删除操作。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * 根据用户 ID 获取用户信息。
     *
     * @param userId 用户 ID
     * @return 用户 DTO
     * @throws BusinessException 如果用户不存在
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        log.debug("Fetching user by ID: {}", userId);
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在，ID: " + userId));
        return convertToDto(userEntity);
    }

    /**
     * 根据用户名获取用户信息。
     *
     * @param username 用户名
     * @return 用户 DTO
     * @throws BusinessException 如果用户不存在
     */
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在，用户名: " + username));
        return convertToDto(userEntity);
    }

    /**
     * 更新用户信息（示例：仅允许更新邮箱）。
     *
     * @param userId      用户 ID
     * @param newEmail    新的邮箱地址
     * @return 更新后的用户 DTO
     * @throws BusinessException 如果用户不存在或邮箱已被使用
     */
    @Transactional
    public UserDto updateUserEmail(Long userId, String newEmail) {
        log.debug("Updating email for user ID: {} to {}", userId, newEmail);
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在，ID: " + userId));

        // 检查新邮箱是否已被其他用户使用
        if (userRepository.existsByEmail(newEmail) && !userEntity.getEmail().equals(newEmail)) {
            throw new BusinessException("邮箱已被其他用户使用: " + newEmail);
        }

        userEntity.setEmail(newEmail);
        UserEntity savedUser = userRepository.save(userEntity);
        log.info("Email updated successfully for user ID: {}", userId);
        return convertToDto(savedUser);
    }

    /**
     * 删除用户。
     *
     * @param userId 用户 ID
     * @throws BusinessException 如果用户不存在
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.debug("Deleting user by ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在，ID: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("User deleted successfully, ID: {}", userId);
    }

    /**
     * 将 UserEntity 转换为 UserDto。
     *
     * @param userEntity 用户实体
     * @return 用户 DTO
     */
    private UserDto convertToDto(UserEntity userEntity) {
        return UserDto.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .createdAt(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .build();
    }
}