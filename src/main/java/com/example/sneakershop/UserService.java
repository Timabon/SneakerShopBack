package com.example.sneakershop;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public void addUser(User user){
        userRepository.save(user);
    }
}
