package com.example.sneakershop.security;

import com.example.sneakershop.user.User;
import com.example.sneakershop.user.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
  private static final int MINIMUM_PASSWORD_LENGTH = 8;
  private final Map<String, ResetRequest> resetRequests = new ConcurrentHashMap<>();
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public PasswordService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public String createResetToken(String email) {
    String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
    String token = UUID.randomUUID().toString();
    User user = userRepository.findByEmail(normalizedEmail);

    if (user != null) {
      resetRequests.put(
          token,
          new ResetRequest(normalizedEmail, Instant.now().plus(15, ChronoUnit.MINUTES)));
    }

    return token;
  }

  public boolean resetPassword(String token, String newPassword) {
    if (token == null
        || newPassword == null
        || newPassword.length() < MINIMUM_PASSWORD_LENGTH) {
      return false;
    }

    ResetRequest request = resetRequests.remove(token);
    if (request == null || request.expiresAt().isBefore(Instant.now())) {
      return false;
    }

    User user = userRepository.findByEmail(request.email());
    if (user == null) {
      return false;
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
    return true;
  }

  private record ResetRequest(String email, Instant expiresAt) {}
}
