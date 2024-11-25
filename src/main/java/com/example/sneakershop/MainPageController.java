package com.example.sneakershop;

import com.example.sneakershop.user.User;
import com.example.sneakershop.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/")
@CrossOrigin(origins = "http://localhost:3000")
public class MainPageController {

    @Autowired
    UserService userService;

    @GetMapping("main")
    public ResponseEntity<String> mainPage() {
        return ResponseEntity.ok("Hello it works");
    }

    @GetMapping("users")
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }
    @PostMapping("addUser")
    public void addUser(@RequestBody User user){
       userService.addUser(user);
    }

        @PostMapping("/login")
        public Map<String, String> submitUsername(@RequestBody Map<String, String> payload) {
            String username = payload.get("username");
            // Process the username here (e.g., save to database, etc.)

            // Send a response back to the client
            Map<String, String> response = new HashMap<>();
            response.put("message", "Username received: " + username);
            User logged_in = new User(username);
            userService.addUser(logged_in);
            return response;
        }

}
