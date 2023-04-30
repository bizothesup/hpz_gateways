/*
 * Copyright (c) 2023. Hypnoz Tech.
 */

package net.hypnoz.hgw.config;

import net.hypnoz.hgw.utils.constances.HypnozProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({HypnozProperties.class})
public class PropertiesConfiguration {
}
