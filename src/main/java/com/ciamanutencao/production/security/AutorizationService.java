package com.ciamanutencao.production.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ciamanutencao.production.repositories.UserRepository;

@Service
public class AutorizationService implements UserDetailsService {

    private final UserRepository userRepository;

    public AutorizationService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        UserDetails user = userRepository.findByLogin(username);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado com o login: " + username);
        }
        
        return user;
    }

}
