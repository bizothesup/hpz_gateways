
package net.hypnoz.hpzgs.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EurekaWorkaroundConfiguration implements HealthIndicator {

    private boolean applicationIsUp = false;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        this.applicationIsUp = true;
    }

    @Override
    public Health health() {
        if (!applicationIsUp) {
            return Health.down().build();
        }
        return Health.up().build();
    }
}
