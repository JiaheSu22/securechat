package com.eric.securechat.friendship.web;

import com.eric.securechat.friendship.dto.FriendRequestDto;
import com.eric.securechat.friendship.dto.FriendRequestViewDto;
import com.eric.securechat.friendship.dto.FriendStatusDto;
import com.eric.securechat.friendship.domain.Friendship;
import com.eric.securechat.friendship.application.FriendshipService;
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

/**
 * REST Controller for handling friendship-related operations.
 * Provides endpoints for managing friend requests, friend lists, and user blocking functionality.
 * All endpoints require user authentication and are accessible under /api/friendships.
 */
@RestController
@RequestMapping("/api/friendships")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    /**
     * Endpoint to send a friend request to another user.
     * The requester is automatically determined from the authenticated user's context.
     * 
     * @param userDetails Spring Security provided current logged-in user information
     * @param payload Request body containing the addressee's username
     * @return ResponseEntity with success or failure response
     */
    @PostMapping("/requests")
    public ResponseEntity<?> sendFriendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> payload) {

        try {
            String requesterUsername = userDetails.getUsername();
            String addresseeUsername = payload.get("addresseeUsername");

            if (addresseeUsername == null || addresseeUsername.isBlank()) {
                return ResponseEntity.badRequest().body("addresseeUsername must be provided.");
            }

            friendshipService.sendRequest(requesterUsername, addresseeUsername);

            return ResponseEntity.status(201).body(Map.of("message", "Friend request sent successfully."));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint to accept a friend request from another user.
     * 
     * @param requestDto The request containing the requester's username
     * @param principal The current authenticated user
     * @return ResponseEntity with success or error response
     */
    @PutMapping("/requests/accept")
    public ResponseEntity<?> acceptFriendRequest(@RequestBody FriendRequestDto requestDto, Principal principal) {
        try {
            String addresseeUsername = principal.getName();
            String requesterUsername = requestDto.getUsername();

            Friendship updatedFriendship = friendshipService.acceptRequest(requesterUsername, addresseeUsername);

            String status = updatedFriendship.getStatus().name();

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
     * Endpoint to decline a friend request from another user.
     * 
     * @param requestDto The request containing the requester's username
     * @param principal The current authenticated user
     * @return ResponseEntity with success or error response
     */
    @PutMapping("/requests/decline")
    public ResponseEntity<?> declineFriendRequest(@RequestBody FriendRequestDto requestDto, Principal principal) {
        try {
            String addresseeUsername = principal.getName();
            String requesterUsername = requestDto.getUsername();

            Friendship updatedFriendship = friendshipService.declineRequest(requesterUsername, addresseeUsername);

            String status = updatedFriendship.getStatus().name();

            return ResponseEntity.ok(Map.of(
                    "message", "Friend request from " + requesterUsername + " declined.",
                    "status", status
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint to retrieve all pending friend requests for the current user.
     * 
     * @param principal The current authenticated user
     * @return ResponseEntity containing a list of pending friend request DTOs
     */
    @GetMapping("/requests/pending")
    public ResponseEntity<List<FriendRequestViewDto>> getPendingRequests(Principal principal) {
        String currentUsername = principal.getName();
        List<FriendRequestViewDto> requests = friendshipService.getPendingRequests(currentUsername);
        return ResponseEntity.ok(requests);
    }

    /**
     * Endpoint to retrieve the current user's friend list with friendship status.
     * 
     * @return ResponseEntity containing a list of friends with their status
     */
    @GetMapping("/my-friends")
    public ResponseEntity<List<FriendStatusDto>> getMyFriends() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        List<FriendStatusDto> friendsWithStatus = friendshipService.getFriendsList(currentUsername);

        return ResponseEntity.ok(friendsWithStatus);
    }

    /**
     * Endpoint to remove a user from the current user's friend list.
     * 
     * @param request The request containing the friend's username to remove
     * @return ResponseEntity with success or error response
     */
    @DeleteMapping("/unfriend")
    public ResponseEntity<?> unfriend(@RequestBody FriendRequestDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        String friendUsername = request.getUsername();

        if (currentUsername.equals(friendUsername)) {
            return ResponseEntity.badRequest().body(Map.of("error", "You cannot unfriend yourself."));
        }

        try {
            friendshipService.unfriend(currentUsername, friendUsername);

            return ResponseEntity.ok(Map.of("message", "Friend '" + friendUsername + "' removed successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint to block a user, preventing any interaction between the users.
     * 
     * @param requestDto The request containing the username to block
     * @param principal The current authenticated user
     * @return ResponseEntity with success or error response
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

    /**
     * Endpoint to unblock a previously blocked user.
     * 
     * @param requestDto The request containing the username to unblock
     * @param principal The current authenticated user
     * @return ResponseEntity with success or error response
     */
    @PostMapping("/unblock")
    public ResponseEntity<?> unblockUser(@RequestBody FriendRequestDto requestDto, Principal principal) {
        try {
            String currentUserUsername = principal.getName();
            String userToUnblockUsername = requestDto.getUsername();

            Friendship result = friendshipService.unblockUser(currentUserUsername, userToUnblockUsername);

            return ResponseEntity.ok().body("Successfully unblocked user: " + userToUnblockUsername);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
