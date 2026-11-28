package br.com.obras.equipamento.controller;

import br.com.obras.shared.security.JwtService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@TestConfiguration
@EnableMethodSecurity
public class MetodoSegurancaTestConfig {

    @MockBean
    private JwtService jwtService;
}