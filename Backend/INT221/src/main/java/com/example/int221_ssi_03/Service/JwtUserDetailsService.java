package com.example.int221_ssi_03.Service;

import com.example.int221_ssi_03.Entities.AuthUserDetail;
import com.example.int221_ssi_03.Entities.User;
import com.example.int221_ssi_03.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("user : " + username + " not found"));
        return new AuthUserDetail(user.getId(), user.getEmail(), user.getPassword(), user.getNickname(), user.getIsActive()
                , getAuthorities(user.getRole()));
    }

    public static List<GrantedAuthority> getAuthorities(String rolesAsCommaSeparated) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Arrays.asList(rolesAsCommaSeparated.split(",")).forEach(
                role-> authorities.add(getAuthority(role))
        );
        return authorities;
    }

    private static GrantedAuthority getAuthority(String role) {
        return new SimpleGrantedAuthority(role);
    }
}

