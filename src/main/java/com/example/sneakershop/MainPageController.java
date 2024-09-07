package com.example.sneakershop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/")
@CrossOrigin(origins = "http://localhost:4200")
public class MainPageController {

    @Autowired
    UserService userService;

    @GetMapping("main")
    public ResponseEntity<String> mainPage() {
        return ResponseEntity.ok("Hello it works");
    }

    @PostMapping("addUser")
    public void addUser(@RequestBody User user){
       userService.addUser(user);
    }
}
