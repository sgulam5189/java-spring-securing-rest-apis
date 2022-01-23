package io.jzheaux.springsecurity.resolutions;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserRepositoryUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public UserRepositoryUserDetailsService(UserRepository users){
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return this.users.findByUsername(username)
                .map(BridgeUser::new)
                .orElseThrow( () -> new UsernameNotFoundException("invalid user"));
    }

    private class BridgeUser extends User implements UserDetails {
        public BridgeUser(User user){
            super(user);
        }

        @Override
        public List<GrantedAuthority> getAuthorities() {
            return this.userAuthorities.stream()
                    .map(UserAuthority::getAuthority)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        @Override
        public boolean isAccountNonExpired() {
            return this.enabled;
        }

        @Override
        public boolean isAccountNonLocked() {
            return this.enabled;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return this.enabled;
        }
    }
}
