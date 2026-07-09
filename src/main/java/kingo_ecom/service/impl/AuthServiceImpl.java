package kingo_ecom.service.impl;

import kingo_ecom.dto.auth.AuthRequestDTO;


import kingo_ecom.dto.auth.AuthResponseDTO;
import kingo_ecom.entity.User;
import kingo_ecom.repository.UserRepository;
import kingo_ecom.security.JwtTokenProvider;
import kingo_ecom.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
public AuthResponseDTO register(AuthRequestDTO request) {
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new IllegalArgumentException("Cet email est déjà utilisé");
    }

    User user = new User();
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    userRepository.save(user);

    // CORRECTION : À l'inscription, pas d'objet Authentication. On utilise la méthode par String/Username
    String token = jwtTokenProvider.generateTokenFromUsername(user.getEmail());
    return new AuthResponseDTO(token);
}

@Override
public AuthResponseDTO login(AuthRequestDTO request) {
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    // CORRECTION : Au login, on passe directement l'objet 'authentication' généré
    String token = jwtTokenProvider.generateToken(authentication);
    return new AuthResponseDTO(token);
}
}

