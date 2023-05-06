package net.hypnoz.hpzgs.config;

import net.hypnoz.hpzgs.security.AuthoritiesConstants;
import net.hypnoz.hpzgs.security.jwt.JWTFilter;
import net.hypnoz.hpzgs.security.jwt.TokenProvider;
import net.hypnoz.hpzgs.utils.conf.HypnozProperties;
import net.hypnoz.hpzgs.web.filter.SpaWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.zalando.problem.spring.webflux.advice.security.SecurityProblemSupport;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Import(SecurityProblemSupport.class)
@Configuration
public class SecurityConfiguration {
    private final HypnozProperties hypnozProperties;
    private final TokenProvider tokenProvider;

    private final ReactiveAuthenticationManager reactiveAuthenticationManager;
    private final SecurityProblemSupport problemSupport;

    public SecurityConfiguration(HypnozProperties hypnozProperties, TokenProvider tokenProvider, ReactiveAuthenticationManager reactiveAuthenticationManager, SecurityProblemSupport problemSupport) {
        this.hypnozProperties = hypnozProperties;
        this.tokenProvider = tokenProvider;
        this.reactiveAuthenticationManager = reactiveAuthenticationManager;
        this.problemSupport = problemSupport;
    }


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){
        http
                .securityMatcher(new NegatedServerWebExchangeMatcher(new OrServerWebExchangeMatcher(
                        pathMatchers("/app/**", "/_app/**", "/i18n/**", "/img/**", "/content/**", "/swagger-ui/**", "/v3/api-docs/**", "/test/**"),
                        pathMatchers(HttpMethod.OPTIONS, "/**")
                )))
                .csrf()
                .disable()
                .addFilterAt(new SpaWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAt(new JWTFilter(tokenProvider), SecurityWebFiltersOrder.HTTP_BASIC)
                .authenticationManager(reactiveAuthenticationManager)
                .exceptionHandling()
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport)
                .and()
                .headers()
                .contentSecurityPolicy(hypnozProperties.getSecurity().getContentSecurityPolicy())
                .and()
                .referrerPolicy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                .and()
                .permissionsPolicy().policy("camera=(), fullscreen=(self), geolocation=(), gyroscope=(), " +
                        "magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()")
                .and()
                .frameOptions().mode(XFrameOptionsServerHttpHeadersWriter.Mode.DENY)
                .and()
                .authorizeExchange()
                .pathMatchers("/").permitAll()
                .pathMatchers("/*.*").permitAll()
                .pathMatchers("/api/authenticate").permitAll()
                .pathMatchers("/api/register").permitAll()
                .pathMatchers("/api/activate").permitAll()
                .pathMatchers("/api/account/reset-password/init").permitAll()
                .pathMatchers("/api/account/reset-password/finish").permitAll()
                .pathMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .pathMatchers("/api/**").authenticated()
                // microfrontend resources are loaded by webpack without authentication, they need to be public
                .pathMatchers("/services/*/*.js").permitAll()
                .pathMatchers("/services/*/*.js.map").permitAll()
                .pathMatchers("/services/*/v3/api-docs").hasAuthority(AuthoritiesConstants.ADMIN)
                .pathMatchers("/services/**").authenticated()
                .pathMatchers("/management/health").permitAll()
                .pathMatchers("/management/health/**").permitAll()
                .pathMatchers("/management/info").permitAll()
                .pathMatchers("/management/prometheus").permitAll()
                .pathMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN);
        // @formatter:on
        return http.build();

    }
}
