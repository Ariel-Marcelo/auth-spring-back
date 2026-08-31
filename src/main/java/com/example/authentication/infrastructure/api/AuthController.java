package com.example.authentication.infrastructure.api;

import com.example.authentication.infrastructure.api.dto.AuthResponse;
import com.example.authentication.infrastructure.api.dto.LoginRequest;
import com.example.authentication.infrastructure.services.security.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(authenticationService.login(request, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(authenticationService.refreshToken(refreshToken, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authenticationService.logout(refreshToken, response);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // Testing Endpoint 1: Requires 'invoices:read' permission
    @GetMapping("/invoices")
    @PreAuthorize("hasAuthority('invoices:read')")
    public ResponseEntity<Map<String, Object>> getInvoices() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(Map.of(
                "message", "Invoices retrieved successfully",
                "user", auth.getName(),
                "authorities", auth.getAuthorities()
        ));
    }

    // Testing Endpoint 2: Requires 'invoices:delete' permission (Only Admin has this)
    @DeleteMapping("/invoices/{id}")
    @PreAuthorize("hasAuthority('invoices:delete')")
    public ResponseEntity<Map<String, String>> deleteInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("message", "Invoice " + id + " deleted successfully"));
    }
}
