package net.hypnoz.hpzgs.config;

import net.hypnoz.hpzgs.utils.conf.HypnozConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Hooks;


@Configuration
@Profile("!" + HypnozConstants.SPRING_PROFILE_PRODUCTION)
public class ReactorConfiguration {

    public ReactorConfiguration() {
        Hooks.onOperatorDebug();
    }
}
