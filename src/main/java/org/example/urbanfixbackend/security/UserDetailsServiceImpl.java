package org.example.urbanfixbackend.security;

import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.exception.UsuarioNotFoundException;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException(email));
        return new CustomUserDetails(usuario);
    }
}
