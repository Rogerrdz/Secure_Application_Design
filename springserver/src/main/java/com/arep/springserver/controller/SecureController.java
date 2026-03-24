package com.arep.springserver.controller;

import com.arep.springserver.dto.UserDTO;
import com.arep.springserver.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/secure")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SecureController {

    private final UserService userService;

    public SecureController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint protegido: obtener información del usuario actual
     * @param principal Principal autenticado
     * @return Información del usuario
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello(Principal principal) {
        try {
            UserDTO user = userService.findByUsername(principal.getName());
            return ResponseEntity.ok(Map.of(
                    "message", "Recurso protegido accedido correctamente",
                    "user", principal.getName(),
                    "userDetails", user,
                    "timestamp", Instant.now().toString()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener la información del usuario"));
        }
    }

    /**
     * Endpoint protegido: obtener perfil del usuario actual
     * @param principal Principal autenticado
     * @return Perfil del usuario
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(Principal principal) {
        try {
            UserDTO user = userService.findByUsername(principal.getName());
            if (user != null) {
                return ResponseEntity.ok(Map.of(
                        "message", "Perfil del usuario",
                        "profile", user
                ));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener el perfil"));
        }
    }

    /**
     * Endpoint protegido (solo administrador): obtener todos los usuarios
     * @param principal Principal autenticado
     * @return Lista de usuarios
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(Principal principal) {
        try {
            List<UserDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(Map.of(
                    "message", "Lista de usuarios obtenida",
                    "count", users.size(),
                    "users", users,
                    "timestamp", Instant.now().toString()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener la lista de usuarios"));
        }
    }

    /**
     * Endpoint protegido: obtener usuario por ID
     * @param id ID del usuario
     * @param principal Principal autenticado
     * @return Datos del usuario
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id, Principal principal) {
        try {
            UserDTO user = userService.getUserById(id);
            if (user != null) {
                return ResponseEntity.ok(Map.of(
                        "message", "Usuario obtenido",
                        "user", user
                ));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener el usuario"));
        }
    }

    /**
     * Endpoint protegido: obtener estado de la aplicación
     * @param principal Principal autenticado
     * @return Estado de la aplicación
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(Principal principal) {
        try {
            return ResponseEntity.ok(Map.of(
                    "message", "Aplicación funcionando correctamente",
                    "status", "UP",
                    "timestamp", Instant.now().toString(),
                    "authenticatedUser", principal.getName()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener el estado"));
        }
    }
}

