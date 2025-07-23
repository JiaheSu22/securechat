package com.eric.securechat.controller;

import com.eric.securechat.dto.FriendRequestDto;
import com.eric.securechat.dto.FriendRequestViewDto;
import com.eric.securechat.dto.FriendStatusDto;
import com.eric.securechat.dto.MessageResponse;
import com.eric.securechat.dto.UserDto;
import com.eric.securechat.model.Friendship;
import com.eric.securechat.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friendships") // 所有与好友相关的API都以 /api/friendships 开头
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    /**
     * API: 发送好友请求
     * METHOD: POST
     * URL: /api/friendships/requests
     * BODY: { "addresseeUsername": "someuser" }
     *
     * @param userDetails Spring Security 提供的当前登录用户信息
     * @param payload     请求体，包含接收者的用户名
     * @return 返回成功或失败的响应
     */
    @PostMapping("/requests")
    public ResponseEntity<?> sendFriendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> payload) {

        try {
            // 从 Spring Security 的上下文中获取当前登录用户的用户名
            String requesterUsername = userDetails.getUsername();
            // 从请求体中获取要添加的好友的用户名
            String addresseeUsername = payload.get("addresseeUsername");

            if (addresseeUsername == null || addresseeUsername.isBlank()) {
                return ResponseEntity.badRequest().body("addresseeUsername must be provided.");
            }

            friendshipService.sendRequest(requesterUsername, addresseeUsername);

            // 返回 201 Created 状态，表示资源已成功创建（这里是好友请求）
            // 并附带一条成功的消息
            return ResponseEntity.status(201).body(Map.of("message", "Friend request sent successfully."));

        } catch (Exception e) {
            // 捕获 Service 层抛出的所有异常（如用户不存在、已是好友等）
            // 返回 400 Bad Request，并在响应体中包含具体的错误信息
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 接受好友请求
     * PUT /api/friendships/requests/accept
     * Body: { "username": "eric" }
     */
    @PutMapping("/requests/accept")
    public ResponseEntity<?> acceptFriendRequest(@RequestBody FriendRequestDto requestDto, Principal principal) {
        try {
            String addresseeUsername = principal.getName();
            String requesterUsername = requestDto.getUsername();

            // 1. 调用正确的 service 方法，它会返回一个 Friendship 对象
            Friendship updatedFriendship = friendshipService.acceptRequest(requesterUsername, addresseeUsername);

            // 2. 从返回的对象中获取状态，并转换为字符串
            String status = updatedFriendship.getStatus().name(); // .name() 将枚举转为字符串 "ACCEPTED"

            return ResponseEntity.ok(Map.of(
                    "message", "Friend request from " + requesterUsername + " accepted.",
                    "status", status
            ));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 拒绝好友请求
     * PUT /api/friendships/requests/decline  (路径改为 decline 更符合方法名)
     * Body: { "username": "eric" }
     */
    @PutMapping("/requests/decline")
    public ResponseEntity<?> declineFriendRequest(@RequestBody FriendRequestDto requestDto, Principal principal) {
        try {
            String addresseeUsername = principal.getName();
            String requesterUsername = requestDto.getUsername();

            // 1. 调用正确的 service 方法
            Friendship updatedFriendship = friendshipService.declineRequest(requesterUsername, addresseeUsername);

            // 2. 从返回的对象中获取状态
            String status = updatedFriendship.getStatus().name(); // "DECLINED"

            return ResponseEntity.ok(Map.of(
                    "message", "Friend request from " + requesterUsername + " declined.",
                    "status", status
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取所有待处理的好友请求
     * GET /api/friendships/requests/pending
     */
    @GetMapping("/requests/pending")
    public ResponseEntity<List<FriendRequestViewDto>> getPendingRequests(Principal principal) {
        String currentUsername = principal.getName();
        List<FriendRequestViewDto> requests = friendshipService.getPendingRequests(currentUsername);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/my-friends")
    public ResponseEntity<List<FriendStatusDto>> getMyFriends() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        List<FriendStatusDto> friendsWithStatus = friendshipService.getFriendsList(currentUsername);

        return ResponseEntity.ok(friendsWithStatus);
    }

    // 在 FriendshipController.java 中
    @DeleteMapping("/unfriend")
    public ResponseEntity<?> unfriend(@RequestBody FriendRequestDto request) {
        // Principal principal 参数可以更安全地获取当前用户名
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        String friendUsername = request.getUsername();

        if (currentUsername.equals(friendUsername)) {
            // 使用 Map.of 返回错误信息，保持一致性
            return ResponseEntity.badRequest().body(Map.of("error", "You cannot unfriend yourself."));
        }

        try {
            friendshipService.unfriend(currentUsername, friendUsername);

            // 使用 Map.of 返回成功的响应，就像你建议的那样
            return ResponseEntity.ok(Map.of("message", "Friend '" + friendUsername + "' removed successfully."));
        } catch (Exception e) {
            // 返回具体的错误信息
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 拉黑一个用户
     * POST /api/friendships/block
     * Body: { "username": "user_to_block" }
     */
    @PostMapping("/block")
    public ResponseEntity<?> blockUser(@RequestBody FriendRequestDto requestDto, Principal principal) {
        String blockerUsername = principal.getName();
        String blockedUsername = requestDto.getUsername();

        if (blockerUsername.equals(blockedUsername)) {
            return ResponseEntity.badRequest().body(Map.of("error", "You cannot block yourself."));
        }

        try {
            friendshipService.blockUser(blockerUsername, blockedUsername);
            return ResponseEntity.ok(Map.of("message", "User '" + blockedUsername + "' has been blocked."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/unblock")
    public ResponseEntity<?> unblockUser(@RequestBody FriendRequestDto requestDto, Principal principal) {
        try {
            // 从安全上下文中获取当前用户名
            String currentUserUsername = principal.getName();
            // DTO 中包含了要解除拉黑的用户名
            String userToUnblockUsername = requestDto.getUsername();

            Friendship result = friendshipService.unblockUser(currentUserUsername, userToUnblockUsername);

            // 可以在这里返回一个自定义的成功响应
            return ResponseEntity.ok().body("Successfully unblocked user: " + userToUnblockUsername);

        } catch (Exception e) {
            // 统一的异常处理...
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
