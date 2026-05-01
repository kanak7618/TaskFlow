package com.taskmanager.controller;

import com.taskmanager.dto.DashboardResponse;
import com.taskmanager.dto.ProjectResponse;
import com.taskmanager.entity.User;
import com.taskmanager.service.AuthService;
import com.taskmanager.service.DashboardService;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(dashboardService.getDashboard(user));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<?>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream()
                .map(u -> new java.util.HashMap<String, Object>() {{
                    put("id", u.getId());
                    put("fullName", u.getFullName());
                    put("email", u.getEmail());
                    put("role", u.getRole());
                    put("createdAt", u.getCreatedAt());
                }})
                .collect(Collectors.toList()));
    }

    @GetMapping("/users/all")
    public ResponseEntity<List<?>> getAllUsersForSelect(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userRepository.findAll().stream()
                .map(u -> new java.util.HashMap<String, Object>() {{
                    put("id", u.getId());
                    put("fullName", u.getFullName());
                    put("email", u.getEmail());
                    put("role", u.getRole());
                }})
                .collect(Collectors.toList()));
    }
}
