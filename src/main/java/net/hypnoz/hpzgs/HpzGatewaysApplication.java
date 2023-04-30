package net.hypnoz.hpzgs;

import jakarta.annotation.PostConstruct;
import net.hypnoz.hpzgs.config.ApplicationProperties;
import net.hypnoz.hpzgs.config.CRLFLogConverter;
import net.hypnoz.hpzgs.domain.Users;
import net.hypnoz.hpzgs.repository.UsersRepository;
import net.hypnoz.hpzgs.utils.conf.DefaultProfileUtil;
import net.hypnoz.hpzgs.utils.conf.HypnozConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@SpringBootApplication
@EnableConfigurationProperties({LiquibaseProperties.class, ApplicationProperties.class})
public class HpzGatewaysApplication {
    @Autowired
    private UsersRepository repository;

    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(HpzGatewaysApplication.class);
    private final Environment env;

    public HpzGatewaysApplication(PasswordEncoder passwordEncoder, Environment env) {
        this.passwordEncoder = passwordEncoder;
        this.env = env;
    }

    @PostConstruct
    public void initApplication() {
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (
                activeProfiles.contains(HypnozConstants.SPRING_PROFILE_DEVELOPMENT) &&
                        activeProfiles.contains(HypnozConstants.SPRING_PROFILE_PRODUCTION)
        ) {
            log.error("You have misconfigured your application! It should not run " + "with both the 'dev' and 'prod' profiles at the same time.");
        }
        if (
                activeProfiles.contains(HypnozConstants.SPRING_PROFILE_DEVELOPMENT) &&
                        activeProfiles.contains(HypnozConstants.SPRING_PROFILE_CLOUD)
        ) {
            log.error(
                    "You have misconfigured your application! It should not " + "run with both the 'dev' and 'cloud' profiles at the same time."
            );
        }

    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(HpzGatewaysApplication.class);
        DefaultProfileUtil.addDefaultProfile(app);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);

    }

    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
        String serverPort = env.getProperty("server.port");
        String contextPath = Optional
                .ofNullable(env.getProperty("server.servlet.context-path"))
                .filter(StringUtils::isNotBlank)
                .orElse("/");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }
        log.info(
                CRLFLogConverter.CRLF_SAFE_MARKER,
                "\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\t{}://localhost:{}{}\n\t" +
                        "External: \t{}://{}:{}{}\n\t" +
                        "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                serverPort,
                contextPath,
                protocol,
                hostAddress,
                serverPort,
                contextPath,
                env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles()
        );
    }

}
