/*
 * Copyright (c) 2023. Hypnoz Tech.
 */

package net.hypnoz.hpzgs.utils.conf;

public interface HypnozConstants {
    String SPRING_PROFILE_DEVELOPMENT = "dev";
    String SPRING_PROFILE_TEST = "test";
    String SPRING_PROFILE_E2E = "e2e";
    String SPRING_PROFILE_PRODUCTION = "prod";
    String SPRING_PROFILE_CLOUD = "cloud";
    String SPRING_PROFILE_HEROKU = "heroku";
    String SPRING_PROFILE_AWS_ECS = "aws-ecs";
    String SPRING_PROFILE_AZURE = "azure";
    String SPRING_PROFILE_API_DOCS = "api-docs";
    String SPRING_PROFILE_NO_LIQUIBASE = "no-liquibase";
    String SPRING_PROFILE_K8S = "k8s";

    String LOGIN_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";

    String SYSTEM = "system";
    String DEFAULT_LANGUAGE = "fr";
}
