package net.hypnoz.hpzgs.web.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import net.hypnoz.hpzgs.security.jwt.JWTFilter;
import net.hypnoz.hpzgs.security.jwt.TokenProvider;
import net.hypnoz.hpzgs.web.rest.vm.LoginVM;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class AuthentificationController {
    private final TokenProvider tokenProvider;
    private final ReactiveAuthenticationManager reactiveAuthenticationManager;

    public AuthentificationController(TokenProvider tokenProvider,
                                      ReactiveAuthenticationManager reactiveAuthenticationManager) {
        this.tokenProvider = tokenProvider;
        this.reactiveAuthenticationManager = reactiveAuthenticationManager;
    }

    @PostMapping("/authenticate")
    public Mono<ResponseEntity<JWTToken>> authenticationJwt(@Valid @RequestBody Mono<LoginVM> loginVM){
        return loginVM
                .flatMap(login->
                        reactiveAuthenticationManager
                                .authenticate(new UsernamePasswordAuthenticationToken(login.getLogin(),login.getPassword()))
                                .flatMap(auth->Mono.fromCallable(()-> tokenProvider.createToken(auth, login.isRememberMe())))
                )
                .map(jwt->{
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
                    return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
                });
    }


    static class JWTToken {

        private String idToken;

        JWTToken(String idToken) {
            this.idToken = idToken;
        }

        @JsonProperty("id_token")
        String getIdToken() {
            return idToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
}
