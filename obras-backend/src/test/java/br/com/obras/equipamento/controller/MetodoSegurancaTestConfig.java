package br.com.obras.equipamento.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/** Liga o {@code @PreAuthorize} nos testes de slice de controller. */
@TestConfiguration
@EnableMethodSecurity
class MetodoSegurancaTestConfig {
}