package com.example.issue15.profile.controller;

import com.example.issue15.profile.dto.*;
import com.example.issue15.profile.service.ProfileService;
import com.example.issue15.profile.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器
 * 处理用户注册、登录、信息管理等HTTP请求
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     * POST /api/profile/register
     *
     * @param registerRequest 注册请求体，包含用户名、密码、邮箱等信息
     * @return 注册结果，包含用户信息和JWT Token
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // 调用服务层进行用户注册
            UserDTO userDTO = profileService.register(registerRequest);
            
            // 生成JWT Token
            String token = jwtUtil.generateToken(userDTO.getId(), userDTO.getUsername());
            
            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户注册成功");
            response.put("data", userDTO);
            response.put("token", token);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            // 处理注册异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 用户登录
     * POST /api/profile/login
     *
     * @param loginRequest 登录请求体，包含用户名和密码
     * @return 登录结果，包含用户信息和JWT Token
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 调用服务层进行用户登录验证
            UserDTO userDTO = profileService.login(loginRequest);
            
            // 生成JWT Token
            String token = jwtUtil.generateToken(userDTO.getId(), userDTO.getUsername());
            
            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户登录成功");
            response.put("data", userDTO);
            response.put("token", token);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 处理登录异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * 获取当前用户信息
     * GET /api/profile/me
     * 需要JWT Token认证
     *
     * @param userId 从JWT Token中提取的用户ID
     * @return 当前用户详细信息
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestAttribute("userId") Long userId) {
        try {
            // 调用服务层获取用户信息
            UserDTO userDTO = profileService.getUserById(userId);
            
            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", userDTO);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 处理获取用户信息异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * 更新用户信息
     * PUT /api/profile/update
     * 需要JWT Token认证
     *
     * @param userId 从JWT Token中提取的用户ID
     * @param updateRequest 更新请求体，包含要更新的用户信息
     * @return 更新后的用户信息
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateProfileRequest updateRequest) {
        try {
            // 调用服务层更新用户信息
            UserDTO userDTO = profileService.updateProfile(userId, updateRequest);
            
            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户信息更新成功");
            response.put("data", userDTO);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 处理更新异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 修改密码
     * PUT /api/profile/change-password
     * 需要JWT Token认证
     *
     * @param userId 从JWT Token中提取的用户ID
     * @param changePasswordRequest 修改密码请求体，包含旧密码和新密码
     * @return 操作结果
     */
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            // 调用服务层修改密码
            profileService.changePassword(userId, changePasswordRequest);
            
            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "密码修改成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 处理修改密码异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 注销用户
     * DELETE /api/profile/delete
     * 需要JWT Token认证
     *
     * @param userId 从JWT Token中提取的用户ID
     * @return 操作结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteAccount(@RequestAttribute("userId") Long userId) {
        try {
            // 调用服务层注销用户
            profileService.deleteUser(userId);
            
            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户账号已注销");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 处理注销异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 刷新JWT Token
     * POST /api/profile/refresh-token
     * 需要旧的JWT Token
     *
     * @param refreshTokenRequest 刷新Token请求体，包含旧的Token
     * @return 新的JWT Token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            // 验证并刷新Token
            String newToken = jwtUtil.refreshToken(refreshTokenRequest.getToken());
            
            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", newToken);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 处理Token刷新异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}
