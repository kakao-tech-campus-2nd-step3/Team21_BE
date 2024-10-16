package com.potatocake.everymoment.security;

import static com.potatocake.everymoment.constant.Constants.DEFAULT_PASSWORD;

import com.potatocake.everymoment.entity.Member;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@RequiredArgsConstructor
public class MemberDetails implements UserDetails {

    private final Member member;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return passwordEncoder.encode(DEFAULT_PASSWORD);
    }

    @Override
    public String getUsername() {
        return String.valueOf(member.getNumber());
    }

    public Long getId() {
        return member.getId();
    }

}
