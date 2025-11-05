package com.example.int221_ssi_03.Entities;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class AuthUserDetail extends org.springframework.security.core.userdetails.User {
    private Integer id;
    private String nickname;
    private boolean isActive;

    public AuthUserDetail(Integer id, String username, String password, String nickname, boolean isActive) {
        this(id, username, password, nickname, isActive,new ArrayList<GrantedAuthority>());
    }

    public AuthUserDetail(Integer id, String username, String password, String nickname, boolean isActive
            , Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.nickname = nickname;
        this.isActive = isActive;
    }
}
