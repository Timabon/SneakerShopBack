package com.example.sneakershop.user;

import com.example.sneakershop.basket.Basket;
import com.example.sneakershop.basket.BasketService;
import com.example.sneakershop.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BasketService basketService;
    private PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, BasketService basketService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.basketService = basketService;
        this.passwordEncoder = passwordEncoder;
    }


    public List<User> getAllUsers() {
       return userRepository.findAll();
    }

    public User createUser(User user) {
        Basket basket = basketService.createBasket();
        user.setBasket(basket);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    // Get a user by email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Update user information
    public User updateUser(Long id, User userDetails) {
        Optional<User> user = userRepository.findById(id); //Optional because user could be null
        if (user.isPresent()) {
            User updatedUser = user.get();
            updatedUser.setName(userDetails.getName());
            updatedUser.setPassword(userDetails.getPassword());
            return userRepository.save(updatedUser);
        }
        return null;
    }

    // Delete a user by ID
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
