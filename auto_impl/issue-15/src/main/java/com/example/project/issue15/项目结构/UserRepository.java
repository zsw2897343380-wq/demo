package com.example.usermanagement.user.repository;

import com.example.usermanagement.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层接口，继承 JpaRepository 提供基本的 CRUD 操作。
 * 自定义查询方法遵循 Spring Data JPA 命名规范。
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 根据用户名查找用户。
     *
     * @param username 用户名
     * @return 包含用户实体的 Optional 对象
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * 根据邮箱查找用户。
     *
     * @param email 邮箱地址
     * @return 包含用户实体的 Optional 对象
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * 检查用户名是否已存在。
     *
     * @param username 用户名
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已存在。
     *
     * @param email 邮箱地址
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByEmail(String email);
}