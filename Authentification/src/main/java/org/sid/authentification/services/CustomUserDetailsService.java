    package org.sid.authentification.services;

    import org.sid.authentification.entities.User;
    import org.sid.authentification.repositories.UserRepository;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.stereotype.Service;

    import java.util.Collections;

    @Service
    public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        public CustomUserDetailsService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            // Récupérer l'utilisateur depuis la base de données
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Mapper le rôle en SimpleGrantedAuthority
            var authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getName()));


            // Retourner un objet User de Spring Security avec un seul rôle
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    authorities
            );
        }
    }
