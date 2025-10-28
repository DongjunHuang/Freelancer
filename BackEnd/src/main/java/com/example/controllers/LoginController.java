package com.example.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.example.requests.User;
import com.example.services.UserService;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class LoginController {
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    /**
     * The message service.
     */
    private UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * To validate the user log in token, it usually needed when the app started and the login token is not loaded to the
     * memory.
     *
     * @return if the request comes into this API, it should be validated.
     */
    @GetMapping("/user/validateLoginToken")
    public ResponseEntity<String> validateLoginToken() {
        // The token should be validated once the request reaches here.
        logger.info("Validating the user");
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/user/login")
    public ResponseEntity<String> updateUserInfo(@RequestBody User user) {
        try {
            // The process is running in the same thread.
            // TODO: this.userService.updateUserInfo(user);
            return ResponseEntity.ok("Success");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Error when updating the message" + ex.toString());
        }
    }
}
