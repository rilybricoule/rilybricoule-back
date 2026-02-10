package com.sbsolutions.rilybricoule.security;

import com.sbsolutions.rilybricoule.entity.User;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        var authorities = user.getRoles().stream()
                .flatMap(role -> {
                    var roleAuthority = new SimpleGrantedAuthority(role.getRoleName().name());
                    var permissionAuthorities = role.getPermissions().stream()
                            .map(permission -> new SimpleGrantedAuthority(permission.getPermissionName().name()));
                    return Stream.concat(
                            Stream.of(roleAuthority),
                            permissionAuthorities
                    );
                })
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                authorities
        );
    }
}
