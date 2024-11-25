package com.example.sneakershop.user;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public void addUser(User user){
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
       return userRepository.findAll();
    }
}
