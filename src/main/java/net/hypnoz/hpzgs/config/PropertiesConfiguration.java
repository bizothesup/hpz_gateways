/*
 * Copyright (c) 2023. Hypnoz Tech.
 */

package net.hypnoz.hpzgs.config;


import net.hypnoz.hpzgs.utils.conf.HypnozProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({HypnozProperties.class})
public class PropertiesConfiguration {
}
