package com.example.sneakershop.user;

import com.example.sneakershop.dto.UserDTO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // tells Spring that it is a Controller
@CrossOrigin(origins = "http://localhost:4200") // allows requests from localhost:4200
@RequestMapping("/api/v1/users") // that is our url after :4200
public class UserController {

  private final IUserService userService;

  public UserController(UserService iUserService) {
    this.userService = iUserService;
  }

  @GetMapping("{id}") // Get the page for url "http://localhost:4200/api/v1/users/{id}"
  public ResponseEntity<User> getUser(@PathVariable Long id) {
    User user = userService.getUserById(id);
    return ResponseEntity.ok(user);
  }

  // Get all users
  @GetMapping
  public ResponseEntity<List<User>> getAllUsers() {
    List<User> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  // Create a new user
  @PostMapping
  public ResponseEntity<User> createUser(@Valid final UserDTO userDTO) {
    final User createdUser = userService.createUser(userDTO);
    return ResponseEntity.ok(createdUser);
  }

  // Get user by email
  @GetMapping("/email/{email}")
  public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
    User user = userService.getUserByEmail(email);
    return (user != null)
        ? ResponseEntity.ok(user)
        : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  // Update a user by ID
  @PutMapping("/{id}")
  public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
    User updatedUser = userService.updateUser(id, userDetails);
    return (updatedUser != null)
        ? ResponseEntity.ok(updatedUser)
        : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  // Delete user by ID
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
