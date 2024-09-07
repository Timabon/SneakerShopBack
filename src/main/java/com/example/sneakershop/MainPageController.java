package com.example.sneakershop;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/")
@CrossOrigin(origins = "http://localhost:4200")
public class MainPageController {

    @GetMapping("main")
    public ResponseEntity<String> mainPage() {
        return ResponseEntity.ok("Hello it works");
    }
}
