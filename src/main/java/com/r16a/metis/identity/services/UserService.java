package com.r16a.metis.identity.services;

import com.r16a.metis._core.audit.AuditService;
import com.r16a.metis._core.exceptions.TenantNotFoundException;
import com.r16a.metis._core.exceptions.UnauthorizedOperationException;
import com.r16a.metis._core.exceptions.UserAlreadyExistsException;
import com.r16a.metis._core.exceptions.UserNotFoundException;
import com.r16a.metis.identity.dto.UserResponse;
import com.r16a.metis.identity.dto.UserUpdateRequest;
import com.r16a.metis.identity.models.Role;
import com.r16a.metis.identity.models.Tenant;
import com.r16a.metis.identity.models.User;
import com.r16a.metis.identity.models.UserRole;
import com.r16a.metis.identity.repositories.RoleRepository;
import com.r16a.metis.identity.repositories.TenantRepository;
import com.r16a.metis.identity.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    /**
     * Retrieves all users in the system.
     * Only Global Admin can access this method.
     *
     * @return list of all users
     */
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToUserResponse);
    }

    public Page<UserResponse> searchUsers(@Nullable String q, Pageable pageable) {
        Specification<User> spec = Specification.unrestricted();

        if (q != null && !q.isBlank()) {
            String trimmed = q.trim();
            UUID idCandidate = null;

            try {
                idCandidate = UUID.fromString(trimmed);
            } catch (IllegalArgumentException ignored) {}

            final UUID finalIdCandidate = idCandidate;
            String like = "%" + trimmed.toLowerCase() + "%";
            Specification<User> idSpec = (root, query, cb) -> finalIdCandidate != null ? cb.equal(root.get("id"), finalIdCandidate) : null;
            Specification<User> emailSpec = (root, query, cb) -> cb.like(cb.lower(root.get("email")), like);
            Specification<User> nameSpec = (root, query, cb) -> cb.like(cb.lower(root.get("name")), like);
            Specification<User> surnameSpec = (root, query, cb) -> cb.like(cb.lower(root.get("surname")), like);
            Specification<User> tenantSpec = (root, query, cb) -> cb.like(cb.lower(root.join("tenant").get("name")), like);

            spec = spec.and(idSpec.or(emailSpec).or(nameSpec).or(surnameSpec).or(tenantSpec));
        }

        return userRepository.findAll(spec, pageable)
                .map(this::convertToUserResponse);
    }

    /**
     * Retrieves all users belonging to a specific tenant.
     * Only Admin users can access this method.
     *
     * @param tenantId the UUID of the tenant
     * @return list of users belonging to the tenant
     * @throws TenantNotFoundException if the tenant is not found
     */
    public Page<UserResponse> getUsersByTenant(UUID tenantId, Pageable pageable) {
        // Verify tenant exists
        if (!tenantRepository.existsById(tenantId)) {
            throw new TenantNotFoundException(tenantId);
        }

        return userRepository.findByTenantId(tenantId, pageable)
                .map(this::convertToUserResponse);
    }

    /**
     * Retrieves a user by their ID.
     * Global Admin and Admin users can access this method.
     *
     * @param id the UUID of the user
     * @return the user response
     * @throws UserNotFoundException if the user is not found
     */
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return convertToUserResponse(user);
    }

    /**
     * Converts a User entity to UserResponse DTO.
     *
     * @param user the user entity
     * @return the user response DTO
     */
    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname())
                .tenantId(user.getTenant() != null ? user.getTenant().getId() : null)
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(java.util.stream.Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

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
     * Creates a new user with the specified roles, enforcing authorization rules.
     * Authorization rules:
     * - Global Admin can create any user with any role
     * - Admin can create users but cannot assign Global Admin role
     * - Other roles cannot create users
     *
     * @param email the email address of the new user
     * @param password the raw password to be encoded and stored
     * @param name the first name of the user
     * @param surname the surname of the user
     * @param tenantId the UUID of the tenant to associate with the user
     * @param roles the roles to assign to the user
     * @return the created user response
     * @throws IllegalArgumentException if the current user is not authorized to create users with the specified roles
     */
    public UserResponse createUser(String email, String password, String name, String surname, UUID tenantId, Set<UserRole> roles) {
        // Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException(email);
        }

        // Get current user's authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedOperationException("User must be authenticated to create users");
        }

        // Check authorization based on current user's roles
        boolean isGlobalAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_GLOBAL_ADMIN"));
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        // Authorization logic
        if (!isGlobalAdmin && !isAdmin) {
            throw new UnauthorizedOperationException("Only Global Admin and Admin users can create new users");
        }

        // Admin cannot create Global Admin users
        if (isAdmin && roles.contains(UserRole.GLOBAL_ADMIN)) {
            throw new UnauthorizedOperationException("Admin users cannot create Global Admin users");
        }

        // Create the user
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setSurname(surname);

        // Set tenant relationship
        if (tenantId != null) {
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new TenantNotFoundException(tenantId));
            user.setTenant(tenant);
        }

        // Set roles
        Set<Role> userRoles = new HashSet<>();
        for (UserRole roleName : roles) {
            Optional<Role> role = roleRepository.findByName(roleName);

            if (role.isPresent()) {
                userRoles.add(role.get());
            } else {
                throw new IllegalArgumentException("Role not found: " + roleName);
            }
        }

        user.setRoles(userRoles);

        User savedUser = userRepository.save(user);
        
        // Audit log the creation
        String tenantUIDStr = tenantId != null ? tenantId.toString() : null;
        auditService.logCreate("User", savedUser.getId(), savedUser, tenantUIDStr);
        
        log.info("Created user: {} with ID: {}", savedUser.getEmail(), savedUser.getId());
        
        return UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .surname(savedUser.getSurname())
                .tenantId(savedUser.getTenant() != null ? savedUser.getTenant().getId() : null)
                .roles(savedUser.getRoles().stream()
                        .map(Role::getName)
                        .collect(java.util.stream.Collectors.toSet()))
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Keep old snapshot for audit
        User oldUser = new User();
        oldUser.setId(user.getId());
        oldUser.setEmail(user.getEmail());
        oldUser.setName(user.getName());
        oldUser.setSurname(user.getSurname());
        oldUser.setTenant(user.getTenant());
        oldUser.setRoles(user.getRoles());
        oldUser.setCreatedAt(user.getCreatedAt());
        oldUser.setUpdatedAt(user.getUpdatedAt());

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getSurname() != null) {
            user.setSurname(request.getSurname());
        }

        if (request.getTenantId() != null) {
            Tenant tenant = tenantRepository.findById(request.getTenantId())
                    .orElseThrow(() -> new TenantNotFoundException(request.getTenantId()));
            user.setTenant(tenant);
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> userRoles = new HashSet<>();
            for (UserRole roleName : request.getRoles()) {
                Optional<Role> role = roleRepository.findByName(roleName);
                role.ifPresent(userRoles::add);
            }
            user.setRoles(userRoles);
        }

        User updated = userRepository.save(user);

        String tenantUIDStr = updated.getTenant() != null ? updated.getTenant().getId().toString() : null;
        auditService.logUpdate("User", updated.getId(), oldUser, updated, tenantUIDStr);

        return convertToUserResponse(updated);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        String tenantUIDStr = user.getTenant() != null ? user.getTenant().getId().toString() : null;

        userRepository.deleteById(id);

        auditService.logDelete("User", id, user, tenantUIDStr);
        log.info("Deleted user with ID: {}", id);
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

        // Set tenant relationship
        if (tenantId != null) {
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new TenantNotFoundException(tenantId));
            user.setTenant(tenant);
        }

        // Set default role (USER)
        Set<Role> roles = new HashSet<>();
        Optional<Role> userRole = roleRepository.findByName(UserRole.USER);
        userRole.ifPresent(roles::add);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        
        // Audit log the registration
        String tenantUIDStr = tenantId != null ? tenantId.toString() : null;
        auditService.logCreate("User", savedUser.getId(), savedUser, tenantUIDStr);
        
        log.info("Registered user: {} with ID: {}", savedUser.getEmail(), savedUser.getId());
        
        return savedUser;
    }
}

