package com.r16a.metis.identity.services;

import com.r16a.metis._core.exceptions.UserAlreadyExistsException;
import com.r16a.metis.identity.models.Role;
import com.r16a.metis.identity.models.User;
import com.r16a.metis.identity.repositories.RoleRepository;
import com.r16a.metis.identity.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Finds a user by their email address.
     * 
     * @param email the email address of the user to find
     * @return the User associated with the given email
     * @throws UsernameNotFoundException if no user is found with the given email
     */
    public User findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }

    /**
     * Registers a new user with the provided details.
     *
     * <p>This method creates a new {@link User} entity, encodes the password,
     * sets the user's name, surname, and timestamps, and assigns the default "USER" role.
     * If a user with the given email already exists, an exception is thrown.</p>
     *
     * @param email the email address of the new user
     * @param password the raw password to be encoded and stored
     * @param name the first name of the user
     * @param surname the surname of the user
     * @param tenantId the UUID of the tenant to associate with the user
     * @return the saved {@link User} entity
     */
    public User registerUser(String email, String password, String name, String surname, UUID tenantId) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException(email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setSurname(surname);

        // TODO: Relationship with tenant
        // Set default role (USER)
        Set<Role> roles = new HashSet<>();
        Optional<Role> userRole = roleRepository.findByName("USER");
        userRole.ifPresent(roles::add);
        user.setRoles(roles);

        return userRepository.save(user);
    }
}

