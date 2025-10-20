package com.r16a.metis.identity.services;

import com.r16a.metis._core.exceptions.InvalidTokenException;
import com.r16a.metis._core.exceptions.UnauthorizedOperationException;
import com.r16a.metis.identity.config.jwt.JwtService;
import com.r16a.metis.identity.config.security.CustomUserDetailsService;
import com.r16a.metis.identity.dto.AuthenticationResponse;
import com.r16a.metis.identity.models.Role;
import com.r16a.metis.identity.models.Tenant;
import com.r16a.metis.identity.models.User;
import com.r16a.metis.identity.models.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final TenantService tenantService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;

    /**
     * Authenticates a user with the provided email and password.
     *
     * <p>This method uses the {@link AuthenticationManager} to authenticate the user credentials.
     * If authentication is successful, it generates and returns a map containing the access token,
     * refresh token, and token type.</p>
     *
     * @param email the email address of the user attempting to authenticate
     * @param password the raw password of the user
     * @return an object with user restricted details and tokens
     * @throws org.springframework.security.core.AuthenticationException if authentication fails
     */
    public AuthenticationResponse authenticate(String email, String password, String tenantDomain) {
        User user = userService.findByEmailOrThrow(email);
        Tenant userTenant = user.getTenant();

        if(userTenant != null && !userTenant.getDomain().equals(tenantDomain)) {
            throw new UnauthorizedOperationException("User is on wrong tenant");
        }

        if(tenantDomain != null && tenantService.getTenantByDomain(tenantDomain) == null) {
            throw new UnauthorizedOperationException("Trying to access to non existent tenant");
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Map<String, String> tokens = buildTokens(userDetails);

        List<String> userRoles = user.getRoles()
                .stream()
                .map(Role::getName)
                .map(UserRole::getDisplayName)
                .collect(Collectors.toList());

        return AuthenticationResponse.builder()
                .username(email)
                .name(user.getName())
                .surname(user.getSurname())
                .roles(userRoles)
                .accessToken(tokens.get("accessToken"))
                .refreshToken(tokens.get("refreshToken"))
                .tokenType(tokens.get("tokenType"))
                .build();
    }

    /**
     * Refreshes the authentication tokens using a valid refresh token.
     *
     * <p>This method validates the provided refresh token. If valid, it extracts the username,
     * loads the user details, and generates new access and refresh tokens.</p>
     *
     * @param refreshToken the refresh token to validate and use for generating new tokens
     * @return a map containing the new access token, refresh token, and token type
     * @throws RuntimeException if the refresh token is invalid
     */
    public Map<String, String> refreshToken(String refreshToken) {
        if (jwtService.validateToken(refreshToken)) {
            String username = jwtService.extractUsername(refreshToken);
            return buildTokens(customUserDetailsService.loadUserByUsername(username));
        }

        throw new InvalidTokenException();
    }

    /**
     * Builds a map containing the access token, refresh token, and token type for the given user details.
     *
     * @param userDetails the user details for which to generate tokens
     * @return a map with keys "accessToken", "refreshToken", and "tokenType"
     */
    private Map<String, String> buildTokens(UserDetails userDetails) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("tokenType", "Bearer");

        return tokens;
    }
}

