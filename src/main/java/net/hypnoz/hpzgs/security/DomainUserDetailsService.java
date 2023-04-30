package net.hypnoz.hpzgs.security;

import net.hypnoz.hpzgs.domain.Authority;
import net.hypnoz.hpzgs.domain.Users;
import net.hypnoz.hpzgs.repository.UsersRepository;
import net.hypnoz.hpzgs.utils.exceptions.UsernameNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component("userDetailsService")
public class DomainUserDetailsService  implements ReactiveUserDetailsService {
    private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);
    private final UsersRepository usersRepository;

    public DomainUserDetailsService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public Mono<UserDetails> findByUsername(final String login) {
        log.debug("Authentification {}",login);
        String lowercaseLogin = login.toLowerCase(Locale.FRENCH);
        return usersRepository
                .findOneWithAuthoritiesByLogin(lowercaseLogin)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database")))
                .map(users->createSecurityUsers(lowercaseLogin,users));
    }

    private User createSecurityUsers(String lowercaseLogin, Users users) {
        if(!users.isActivated()){
            throw new UserNotActivatedException("User " + lowercaseLogin + " was not activated");
        }
        List<GrantedAuthority> grantedAuthorities = users
                .getAuthorities()
                .stream()
                .map(Authority::getName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new User(users.getLogin(),users.getPassword(),grantedAuthorities);
    }
}
