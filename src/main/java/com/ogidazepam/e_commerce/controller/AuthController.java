package com.ogidazepam.e_commerce.controller;

import com.ogidazepam.e_commerce.dto.CustomerLoginDTO;
import com.ogidazepam.e_commerce.dto.CustomerRegisterDTO;
import com.ogidazepam.e_commerce.service.CustomerService;
import com.ogidazepam.e_commerce.service.security.JwtService;
import com.ogidazepam.e_commerce.util.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtService jwtService;
    private final CustomerService customerService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(JwtService jwtService, CustomerService customerService, AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.customerService = customerService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerCustomer(@RequestBody @Valid CustomerRegisterDTO dto){
        customerService.saveCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<String> authCustomer(@RequestBody @Valid CustomerLoginDTO dto){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getEmail(),
                        dto.getPassword()
                )
        );

        String jwt = jwtService.generateToken((CustomUserDetails) authentication.getPrincipal());
        return ResponseEntity.ok(jwt);
    }
}
