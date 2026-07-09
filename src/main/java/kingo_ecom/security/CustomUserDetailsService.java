package kingo_ecom.security;

import kingo_ecom.entity.User;
import kingo_ecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Note importante : Pour que le cast (User) authentication.getPrincipal()
 * utilisé dans les Services (AuthServiceImpl, CartServiceImpl,
 * OrderServiceImpl)
 * fonctionne, l'entité kingo_ecom.entity.User DOIT implémenter
 * l'interface org.springframework.security.core.userdetails.UserDetails.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email));

        // L'entité User doit implémenter UserDetails (getPassword(), getAuthorities(),
        // isAccountNonExpired(), etc.)
        return user;
    }
}