package com.brainbyte.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String SESSION_COOKIE = "brainbyte_session";
    private final AuthService authService;
    private final RateLimiter rateLimiter;
    private final boolean production;

    public AuthController(
            AuthService authService,
            RateLimiter rateLimiter,
            @Value("${NODE_ENV:development}") String environment) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
        this.production = "production".equalsIgnoreCase(environment);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Account>> register(
            @RequestBody AuthRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse response) {
        rateLimiter.check("auth:" + clientAddress(servletRequest), 10, Duration.ofMinutes(15));
        AuthService.AuthenticatedAccount result = authService.register(request.email(), request.password());
        setSessionCookie(response, result.token());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("account", result.account()));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Account>> login(
            @RequestBody AuthRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse response) {
        rateLimiter.check("auth:" + clientAddress(servletRequest), 10, Duration.ofMinutes(15));
        AuthService.AuthenticatedAccount result = authService
                .login(request.email(), request.password())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
        setSessionCookie(response, result.token());
        return ResponseEntity.ok(Map.of("account", result.account()));
    }

    @GetMapping("/me")
    public Map<String, Account> current(HttpServletRequest request) {
        Account account = authService.currentAccount(getSessionToken(request))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please sign in to continue."));
        return Map.of("account", account);
    }

    @PostMapping("/logout")
    public Map<String, Boolean> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(getSessionToken(request));
        response.addHeader("Set-Cookie", ResponseCookie.from(SESSION_COOKIE, "")
                .httpOnly(true)
                .secure(production)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ZERO)
                .build()
                .toString());
        return Map.of("success", true);
    }

    static String getSessionToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> SESSION_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void setSessionCookie(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie", ResponseCookie.from(SESSION_COOKIE, token)
                .httpOnly(true)
                .secure(production)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build()
                .toString());
    }

    private static String clientAddress(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    public record AuthRequest(String email, String password) {}
}
