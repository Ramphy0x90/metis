package com.r16a.metis.identity.controllers;

import com.r16a.metis.identity.dto.*;
import com.r16a.metis.identity.services.UserService;
import com.r16a.metis.identity.models.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.Arrays;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(name = "q", required = false) String query,
            Pageable pageable
    ) {
        Page<UserResponse> users = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsersByTenant(@PathVariable UUID tenantId) {
        List<UserResponse> users = userService.getUsersByTenant(tenantId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Creates a new user with the specified roles.
     * Authorization:
     * - Global Admin can create any user with any role
     * - Admin can create users but cannot assign Global Admin role
     * - Other roles cannot access this endpoint
     */
    @PostMapping
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse user = userService.createUser(
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                request.getSurname(),
                request.getTenantId(),
                request.getRoles()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserResponse updated = userService.updateUser(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('GLOBAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<List<UserRole>> getAvailableRoles() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isGlobalAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_GLOBAL_ADMIN"));

        List<UserRole> roles;
        if (isGlobalAdmin) {
            roles = Arrays.asList(UserRole.values());
        } else {
            roles = new ArrayList<>(Arrays.asList(UserRole.values()));
            roles.remove(UserRole.GLOBAL_ADMIN);
        }

        return ResponseEntity.ok(roles);
    }
}
