package com.example.issue15.user.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户实体类
 * 对应数据库中的用户表，用于存储用户的基本信息
 * 
 * @author issue15
 * @version 1.0
 */
@Entity
@Table(name = "users")
public class User {
    
    /**
     * 用户ID - 主键，自增长
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    /**
     * 用户名 - 唯一，用于登录
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * 密码 - 加密存储（BCrypt加密）
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    /**
     * 邮箱 - 唯一，用于找回密码和通知
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    /**
     * 昵称 - 显示名称
     */
    @Column(name = "nickname", length = 50)
    private String nickname;
    
    /**
     * 手机号 - 可选
     */
    @Column(name = "phone", length = 20)
    private String phone;
    
    /**
     * 头像URL
     */
    @Column(name = "avatar", length = 500)
    private String avatar;
    
    /**
     * 用户状态：0-禁用，1-启用
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 最后登录时间
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    /**
     * 无参构造函数 - JPA要求
     */
    public User() {
    }
    
    /**
     * 带必要参数的构造函数
     * 
     * @param username 用户名
     * @param password 密码（已加密）
     * @param email 邮箱
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
    
    /**
     * 实体持久化前的预处理方法
     * 自动设置创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 实体更新前的预处理方法
     * 自动更新更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // ==================== Getter and Setter 方法 ====================
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    // ==================== equals, hashCode, toString 方法 ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
