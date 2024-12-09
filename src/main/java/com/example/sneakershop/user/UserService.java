package com.example.sneakershop.user;

import com.example.sneakershop.basket.Basket;
import com.example.sneakershop.basket.IBasketService;
import com.example.sneakershop.dto.UserDTO;
import com.example.sneakershop.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {
  private final UserRepository userRepository;
  private final IBasketService IBasketService;
  private PasswordEncoder passwordEncoder;

  public UserService(
      UserRepository userRepository,
      IBasketService IBasketService,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.IBasketService = IBasketService;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  @Override
  public User createUser(UserDTO userDTO) {
    Basket basket = IBasketService.createBasket();
    final User user = new User();
    user.setName(userDTO.getName());
    user.setEmail(userDTO.getEmail());
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setBasket(basket);
    return userRepository.save(user);
  }

  @Override
  public User getUserById(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
  }

  @Override
  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  public User updateUser(Long id, User userDetails) {
    Optional<User> user = userRepository.findById(id); // Optional because user could be null
    if (user.isPresent()) {
      User updatedUser = user.get();
      updatedUser.setName(userDetails.getName());
      updatedUser.setPassword(userDetails.getPassword());
      return userRepository.save(updatedUser);
    }
    return null;
  }

  @Override
  public void deleteUser(Long id) {
    userRepository.deleteById(id);
  }
}
