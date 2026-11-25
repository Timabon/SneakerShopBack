package com.example.sneakershop.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.sneakershop.user.User;
import com.example.sneakershop.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordServiceTest {

  @Test
  void resetsThePasswordForAValidToken() {
    UserRepository userRepository = mock(UserRepository.class);
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    User user = new User();
    user.setEmail("shopper@example.com");
    when(userRepository.findByEmail("shopper@example.com")).thenReturn(user);

    PasswordService passwordService = new PasswordService(userRepository, passwordEncoder);
    String token = passwordService.createResetToken("SHOPPER@example.com");

    assertTrue(passwordService.resetPassword(token, "new-password"));
    assertTrue(passwordEncoder.matches("new-password", user.getPassword()));
    verify(userRepository).save(user);
  }

  @Test
  void rejectsPasswordsShorterThanEightCharacters() {
    UserRepository userRepository = mock(UserRepository.class);
    User user = new User();
    user.setEmail("shopper@example.com");
    when(userRepository.findByEmail("shopper@example.com")).thenReturn(user);

    PasswordService passwordService =
        new PasswordService(userRepository, new BCryptPasswordEncoder());
    String token = passwordService.createResetToken("shopper@example.com");

    assertFalse(passwordService.resetPassword(token, "short"));
  }
}
