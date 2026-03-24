package com.arep.springserver.controller;

import com.arep.springserver.dto.UserDTO;
import com.arep.springserver.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registrar nuevo usuario
     * @param userDTO Datos del usuario
     * @return Usuario creado
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserDTO userDTO) {
        try {
            UserDTO savedUser = userService.register(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Usuario creado exitosamente",
                            "user", savedUser
                    ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar el usuario"));
        }
    }

    /**
     * Iniciar sesión
     * @param loginRequest Credenciales
     * @return Resultado del login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            boolean valid = userService.authenticate(loginRequest.username().trim(), loginRequest.password());
            if (!valid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Credenciales inválidas"));
            }
            return ResponseEntity.ok(Map.of(
                    "message", "Login exitoso",
                    "username", loginRequest.username()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al autenticarse"));
        }
    }

    /**
     * Manejo de excepciones de validación
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    /**
     * Record para validación de login
     */
    public record LoginRequest(
            String username,
            String password) {
    }
}

