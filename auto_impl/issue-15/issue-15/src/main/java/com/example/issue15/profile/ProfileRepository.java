package com.example.issue15.profile.repository;

import com.example.issue15.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户档案数据访问层
 * 负责用户信息的数据库操作，包括用户注册、登录验证、信息管理等
 * 继承 JpaRepository 以获取基本的 CRUD 操作能力
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    /**
     * 根据用户名查找用户档案
     * 用于用户登录验证和用户名唯一性检查
     *
     * @param username 用户名
     * @return 包含用户档案的 Optional 对象
     */
    Optional<Profile> findByUsername(String username);

    /**
     * 根据邮箱查找用户档案
     * 用于邮箱唯一性检查和通过邮箱登录
     *
     * @param email 邮箱地址
     * @return 包含用户档案的 Optional 对象
     */
    Optional<Profile> findByEmail(String email);

    /**
     * 检查用户名是否已存在
     * 用于用户注册时的用户名唯一性验证
     *
     * @param username 用户名
     * @return 如果用户名已存在返回 true，否则返回 false
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已存在
     * 用于用户注册时的邮箱唯一性验证
     *
     * @param email 邮箱地址
     * @return 如果邮箱已存在返回 true，否则返回 false
     */
    boolean existsByEmail(String email);

    /**
     * 根据用户名或邮箱查找用户档案
     * 支持用户使用用户名或邮箱进行登录
     *
     * @param username 用户名
     * @param email 邮箱地址
     * @return 包含用户档案的 Optional 对象
     */
    @Query("SELECT p FROM Profile p WHERE p.username = :username OR p.email = :email")
    Optional<Profile> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    /**
     * 根据用户ID和激活状态查找用户
     * 用于验证用户账户是否已激活
     *
     * @param id 用户ID
     * @param isActive 激活状态
     * @return 包含用户档案的 Optional 对象
     */
    Optional<Profile> findByIdAndIsActive(Long id, Boolean isActive);

    /**
     * 根据用户名和激活状态查找用户
     * 用于登录时验证用户账户状态
     *
     * @param username 用户名
     * @param isActive 激活状态
     * @return 包含用户档案的 Optional 对象
     */
    Optional<Profile> findByUsernameAndIsActive(String username, Boolean isActive);

    /**
     * 根据邮箱和激活状态查找用户
     * 用于邮箱登录时验证用户账户状态
     *
     * @param email 邮箱地址
     * @param isActive 激活状态
     * @return 包含用户档案的 Optional 对象
     */
    Optional<Profile> findByEmailAndIsActive(String email, Boolean isActive);

    /**
     * 统计指定角色下的用户数量
     * 用于系统管理和统计
     *
     * @param role 用户角色
     * @return 用户数量
     */
    long countByRole(String role);

    /**
     * 根据用户名模糊搜索用户
     * 用于用户搜索功能
     *
     * @param username 用户名关键字
     * @return 匹配的用户列表
     */
    @Query("SELECT p FROM Profile p WHERE p.username LIKE %:username%")
    java.util.List<Profile> searchByUsernameLike(@Param("username") String username);

    /**
     * 根据邮箱模糊搜索用户
     * 用于用户搜索功能
     *
     * @param email 邮箱关键字
     * @return 匹配的用户列表
     */
    @Query("SELECT p FROM Profile p WHERE p.email LIKE %:email%")
    java.util.List<Profile> searchByEmailLike(@Param("email") String email);

    /**
     * 更新用户最后登录时间
     * 用于记录用户登录活动
     *
     * @param id 用户ID
     * @param lastLoginTime 最后登录时间
     */
    @Query("UPDATE Profile p SET p.lastLoginTime = :lastLoginTime WHERE p.id = :id")
    void updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") java.time.LocalDateTime lastLoginTime);

    /**
     * 更新用户密码
     * 用于密码重置和修改功能
     *
     * @param id 用户ID
     * @param newPassword 新密码（加密后）
     */
    @Query("UPDATE Profile p SET p.password = :newPassword WHERE p.id = :id")
    void updatePassword(@Param("id") Long id, @Param("newPassword") String newPassword);

    /**
     * 更新用户激活状态
     * 用于账户激活和禁用功能
     *
     * @param id 用户ID
     * @param isActive 激活状态
     */
    @Query("UPDATE Profile p SET p.isActive = :isActive WHERE p.id = :id")
    void updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    /**
     * 查找所有已激活的用户
     * 用于系统管理和用户列表展示
     *
     * @return 已激活的用户列表
     */
    java.util.List<Profile> findByIsActiveTrue();

    /**
     * 查找所有未激活的用户
     * 用于账户审核和管理
     *
     * @return 未激活的用户列表
     */
    java.util.List<Profile> findByIsActiveFalse();

    /**
     * 根据创建时间范围查找用户
     * 用于用户统计和报表生成
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 指定时间范围内创建的用户列表
     */
    @Query("SELECT p FROM Profile p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    java.util.List<Profile> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                                   @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 删除指定用户名的用户
     * 用于账户注销功能
     *
     * @param username 用户名
     */
    void deleteByUsername(String username);

    /**
     * 删除指定邮箱的用户
     * 用于账户注销功能
     *
     * @param email 邮箱地址
     */
    void deleteByEmail(String email);
}
