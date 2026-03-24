package com.arep.springserver.service;

import com.arep.springserver.dto.UserDTO;
import com.arep.springserver.model.User;
import com.arep.springserver.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registrar un nuevo usuario
     * @param userDTO Datos del usuario
     * @return UserDTO del usuario creado
     */
    public UserDTO register(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("El usuario '" + userDTO.getUsername() + "' ya existe");
        }

        User user = convertToEntity(userDTO);
        user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    /**
     * Autenticar usuario
     * @param username Nombre de usuario
     * @param rawPassword Contraseña en texto plano
     * @return true si es válido, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean authenticate(String username, String rawPassword) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.isPresent() && passwordEncoder.matches(rawPassword, user.get().getPasswordHash());
    }

    /**
     * Obtener usuario por nombre de usuario
     * @param username Nombre de usuario
     * @return UserDTO si existe, null en caso contrario
     */
    @Transactional(readOnly = true)
    public UserDTO findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Obtener usuario por ID
     * @param id ID del usuario
     * @return UserDTO si existe, null en caso contrario
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(this::convertToDTO).orElse(null);
    }

    /**
     * Obtener todos los usuarios
     * @return Lista de UserDTO
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar usuario
     * @param id ID del usuario
     * @param userDTO Datos actualizados
     * @return UserDTO actualizado si existe, null en caso contrario
     */
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            if (!existingUser.getUsername().equals(userDTO.getUsername()) &&
                userRepository.existsByUsername(userDTO.getUsername())) {
                throw new IllegalArgumentException("El nombre de usuario '" + userDTO.getUsername() + "' ya existe");
            }

            existingUser.setUsername(userDTO.getUsername());
            existingUser.setEmail(userDTO.getEmail());
            existingUser.setRole(userDTO.getRole());

            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                existingUser.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));
            }

            User updatedUser = userRepository.save(existingUser);
            return convertToDTO(updatedUser);
        }
        return null;
    }

    /**
     * Eliminar usuario
     * @param id ID del usuario
     * @return true si se eliminó, false si no existe
     */
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Convertir entidad User a UserDTO
     * @param user Entidad User
     * @return UserDTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }

    /**
     * Convertir UserDTO a entidad User
     * @param userDTO UserDTO
     * @return Entidad User
     */
    private User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setRole(userDTO.getRole());
        return user;
    }
}

