package com.example.issue15.user.controller;

import com.example.issue15.user.dto.*;
import com.example.issue15.user.service.UserService;
import com.example.issue15.common.util.JwtUtil;
import com.example.issue15.common.exception.BusinessException;
import com.example.issue15.common.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * 用户管理控制器
 * 提供用户注册、登录、信息管理等功能
 * 
 * @author issue15
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@Api(tags = "用户管理接口", description = "用户注册、登录、信息管理")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求DTO
     * @return 注册结果
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "注册新用户并返回JWT Token")
    public ResponseEntity<Result<LoginResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        
        log.info("用户注册请求 - 用户名: {}", registerRequest.getUsername());
        
        try {
            // 调用服务层进行注册
            LoginResponse loginResponse = userService.register(registerRequest);
            
            log.info("用户注册成功 - 用户ID: {}", loginResponse.getUserId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.success("注册成功", loginResponse));
                    
        } catch (BusinessException e) {
            log.warn("用户注册失败 - 用户名: {}, 原因: {}", 
                    registerRequest.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            log.error("用户注册异常 - 用户名: {}", registerRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("注册失败，请稍后重试"));
        }
    }

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求DTO
     * @return 登录结果（包含JWT Token）
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "用户登录并返回JWT Token")
    public ResponseEntity<Result<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        
        log.info("用户登录请求 - 用户名: {}", loginRequest.getUsername());
        
        try {
            // 调用服务层进行登录验证
            LoginResponse loginResponse = userService.login(loginRequest);
            
            log.info("用户登录成功 - 用户ID: {}", loginResponse.getUserId());
            
            return ResponseEntity.ok(Result.success("登录成功", loginResponse));
            
        } catch (BusinessException e) {
            log.warn("用户登录失败 - 用户名: {}, 原因: {}", 
                    loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            log.error("用户登录异常 - 用户名: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("登录失败，请稍后重试"));
        }
    }

    /**
     * 获取当前用户信息
     * 需要JWT Token认证
     * 
     * @param token JWT Token（从请求头获取）
     * @return 用户信息
     */
    @GetMapping("/me")
    @ApiOperation(value = "获取当前用户信息", notes = "获取已登录用户的详细信息")
    public ResponseEntity<Result<UserInfoResponse>> getCurrentUser(
            @RequestHeader("Authorization") 
            @ApiParam(value = "JWT Token", required = true) 
            @NotBlank(message = "Token不能为空") String token) {
        
        log.info("获取当前用户信息请求");
        
        try {
            // 验证Token并提取用户ID
            String jwtToken = token.startsWith("Bearer ") ? 
                    token.substring(7) : token;
            
            if (!jwtUtil.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error("Token无效或已过期"));
            }
            
            Long userId = jwtUtil.getUserIdFromToken(jwtToken);
            
            // 调用服务层获取用户信息
            UserInfoResponse userInfo = userService.getUserInfo(userId);
            
            log.info("获取用户信息成功 - 用户ID: {}", userId);
            
            return ResponseEntity.ok(Result.success("获取成功", userInfo));
            
        } catch (BusinessException e) {
            log.warn("获取用户信息失败 - 原因: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取用户信息失败，请稍后重试"));
        }
    }

    /**
     * 更新用户信息
     * 需要JWT Token认证
     * 
     * @param token JWT Token
     * @param updateRequest 更新请求DTO
     * @return 更新后的用户信息
     */
    @PutMapping("/me")
    @ApiOperation(value = "更新用户信息", notes = "更新当前登录用户的信息")
    public ResponseEntity<Result<UserInfoResponse>> updateUserInfo(
            @RequestHeader("Authorization") 
            @ApiParam(value = "JWT Token", required = true) 
            @NotBlank(message = "Token不能为空") String token,
            @Valid @RequestBody UpdateUserRequest updateRequest) {
        
        log.info("更新用户信息请求");
        
        try {
            // 验证Token并提取用户ID
            String jwtToken = token.startsWith("Bearer ") ? 
                    token.substring(7) : token;
            
            if (!jwtUtil.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error("Token无效或已过期"));
            }
            
            Long userId = jwtUtil.getUserIdFromToken(jwtToken);
            
            // 调用服务层更新用户信息
            UserInfoResponse updatedUser = userService.updateUserInfo(userId, updateRequest);
            
            log.info("更新用户信息成功 - 用户ID: {}", userId);
            
            return ResponseEntity.ok(Result.success("更新成功", updatedUser));
            
        } catch (BusinessException e) {
            log.warn("更新用户信息失败 - 原因: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            log.error("更新用户信息异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("更新用户信息失败，请稍后重试"));
        }
    }

    /**
     * 刷新Token
     * 
     * @param refreshToken 刷新Token
     * @return 新的JWT Token
     */
    @PostMapping("/refresh-token")
    @ApiOperation(value = "刷新Token", notes = "使用刷新Token获取新的JWT Token")
    public ResponseEntity<Result<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest refreshRequest) {
        
        log.info("刷新Token请求");
        
        try {
            // 验证刷新Token
            if (!jwtUtil.validateRefreshToken(refreshRequest.getRefreshToken())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error("刷新Token无效或已过期"));
            }
            
            // 生成新的Token
            TokenRefreshResponse tokenResponse = userService.refreshToken(
                    refreshRequest.getRefreshToken());
            
            log.info("Token刷新成功");
            
            return ResponseEntity.ok(Result.success("Token刷新成功", tokenResponse));
            
        } catch (BusinessException e) {
            log.warn("Token刷新失败 - 原因: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Token刷新异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("Token刷新失败，请稍后重试"));
        }
    }

    /**
     * 用户注销
     * 
     * @param token JWT Token
     * @return 注销结果
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户注销", notes = "注销当前用户登录状态")
    public ResponseEntity<Result<Void>> logout(
            @RequestHeader("Authorization") 
            @ApiParam(value = "JWT Token", required = true) 
            @NotBlank(message = "Token不能为空") String token) {
        
        log.info("用户注销请求");
        
        try {
            String jwtToken = token.startsWith("Bearer ") ? 
                    token.substring(7) : token;
            
            // 调用服务层处理注销逻辑
            userService.logout(jwtToken);
            
            log.info("用户注销成功");
            
            return ResponseEntity.ok(Result.success("注销成功"));
            
        } catch (BusinessException e) {
            log.warn("用户注销失败 - 原因: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            log.error("用户注销异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("注销失败，请稍后重试"));
        }
    }
}

这个控制器实现了以下功能：

1. **用户注册** (`POST /api/v1/users/register`) - 注册新用户并返回JWT Token
2. **用户登录** (`POST /api/v1/users/login`) - 用户登录验证并返回JWT Token
3. **获取当前用户信息** (`GET /api/v1/users/me`) - 获取已登录用户的详细信息
4. **更新用户信息** (`PUT /api/v1/users/me`) - 更新当前登录用户的信息
5. **刷新Token** (`POST /api/v1/users/refresh-token`) - 使用刷新Token获取新的JWT Token
6. **用户注销** (`POST /api/v1/users/logout`) - 注销当前用户登录状态

代码特点：
- 使用`@RestController`和`@RequestMapping`注解
- 包含完整的异常处理
- 使用DTO进行数据传输
- 包含日志记录
- 支持Swagger API文档
- 遵循RESTful API设计规范
- 包含参数验证
- 返回统一的响应格式