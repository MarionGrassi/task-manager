package it.infra.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestTemplate

@TestConfiguration
@Profile("test")
class IntegrationTestConfig {
    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()
}
