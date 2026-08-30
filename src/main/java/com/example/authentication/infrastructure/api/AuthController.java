package com.example.authentication.infrastructure.api;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {


    @GetMapping
    public ResponseEntity<String> auth() {
        return ResponseEntity.ok("Hello World");
    }
}
