package com.example.sneakershop.security;

import com.example.sneakershop.dto.AuthDTO;
import com.example.sneakershop.dto.UserDTO;
import com.example.sneakershop.security.jwt.JwtService;
import com.example.sneakershop.user.IUserService;
import com.example.sneakershop.user.User;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000") // allows requests from localhost:3000
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordService passwordService;
    private final GoogleSignIn googleSignIn;
    @Value("${sneakershop.local-demo.enabled}")
    private boolean localDemo;

    AuthController(IUserService userService, PasswordEncoder passwordEncoder, JwtService jwtService,
            AuthenticationManager authenticationManager, PasswordService passwordService, GoogleSignIn googleSignIn) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.passwordService = passwordService;
        this.googleSignIn = googleSignIn;
    }

    @GetMapping("/profile")
    public Map<String, Object> profile(@AuthenticationPrincipal OAuth2User principal) {
        // returns attributes provided by Google (email, name, picture, etc.)
        return principal == null ? Map.of() : principal.getAttributes();
    }

    @GetMapping("/")
    public String index() {
        return "Welcome. Go to /oauth2/authorization/google to sign in.";
    }

    @GetMapping("/login-success")
    public String loginSuccess() {
        return "Login successful! You can now access protected resources.";
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthDTO request) {
        // authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = JwtService.generateToken(request.getEmail());
        Map<String, String> responseBody = Map.of("token", token);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/google/exchange")
    public ResponseEntity<Map<String, String>> exchangeGoogleCode(@RequestBody GoogleCodeRequest request) {
        return googleSignIn.exchange(request.code(), request.verifier())
                .map(token -> ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("token", token)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/signup")
    public ResponseEntity<User> createUser(@RequestBody @Valid final UserDTO userDTO) {
        final User createdUser = userService.createUser(userDTO);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (!localDemo) {
            return ResponseEntity.notFound().build();
        }
        String token = passwordService.createResetToken(request.email());
        return ResponseEntity.ok(Map.of(
                "message", "Reset request created. The local demo can continue immediately.",
                "resetToken", token
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (!localDemo) {
            return ResponseEntity.notFound().build();
        }
        if (!passwordService.resetPassword(request.token(), request.password())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "The reset link is invalid, expired, or the password is too short."
            ));
        }

        return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
    }

    public record ForgotPasswordRequest(String email) {}

    public record ResetPasswordRequest(String token, String password) {}

    public record GoogleCodeRequest(String code, String verifier) {}
}
