package com.example.sneakershop.user;

import com.example.sneakershop.dto.UserDTO;
import java.util.List;

public interface IUserService {
  List<User> getAllUsers();

  User createUser(UserDTO userDTO);

  User getUserById(Long id);

  // Get a user by email
  User getUserByEmail(String email);

  // Update user information
  User updateUser(Long id, User userDetails);

  // Delete a user by ID
  void deleteUser(Long id);
}
