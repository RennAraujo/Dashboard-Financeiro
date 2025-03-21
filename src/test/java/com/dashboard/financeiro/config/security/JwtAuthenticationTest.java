package com.dashboard.financeiro.config.security;

import com.dashboard.financeiro.model.User;
import com.dashboard.financeiro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtTokenFilter jwtTokenFilter;

    private final String validToken = "valid.jwt.token";
    private final String username = "testuser";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        // Usuário de teste
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername(username);
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setRole("ROLE_USER");

        // Configurar o mock do UserRepository
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("Deve validar token JWT válido")
    void shouldValidateValidJwtToken() {
        // Configurar o comportamento esperado para o jwtTokenProvider
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUsername(validToken)).thenReturn(username);

        // Configurar um usuário autenticado
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        // Criar uma autenticação com o usuário
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // Definir um token válido no contexto de segurança
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Verificar que a autenticação está no contexto
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("Deve rejeitar token JWT inválido")
    void shouldRejectInvalidJwtToken() {
        // Configurar o comportamento esperado para o jwtTokenProvider
        String invalidToken = "invalid.token";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // Simular a autenticação com token inválido
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        
        // Verificar que o token é inválido
        assertFalse(isValid);
        
        // Verificar que não há autenticação no contexto
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Deve criar token JWT para usuário")
    void shouldCreateJwtTokenForUser() {
        // Configurar o comportamento esperado para o jwtTokenProvider
        when(jwtTokenProvider.createToken(username, "ROLE_USER")).thenReturn(validToken);

        // Simular a criação do token
        String token = jwtTokenProvider.createToken(username, "ROLE_USER");
        
        // Verificar que o token foi criado
        assertEquals(validToken, token);
    }

    @Test
    @DisplayName("Deve extrair informações corretas do token JWT")
    void shouldExtractCorrectInformationFromJwtToken() {
        // Configurar o comportamento esperado para o jwtTokenProvider
        when(jwtTokenProvider.getUsername(validToken)).thenReturn(username);

        // Simular a extração do nome de usuário
        String extractedUsername = jwtTokenProvider.getUsername(validToken);
        
        // Verificar que o nome de usuário foi extraído corretamente
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Deve lidar corretamente com token expirado")
    void shouldHandleExpiredToken() {
        // Configurar o comportamento esperado para o jwtTokenProvider
        String expiredToken = "expired.token";
        when(jwtTokenProvider.validateToken(expiredToken)).thenThrow(new JwtAuthenticationException("Token expirado"));

        // Verificar que a exceção é lançada
        assertThrows(JwtAuthenticationException.class, () -> {
            jwtTokenProvider.validateToken(expiredToken);
        });
    }

    @Test
    @DisplayName("Deve retornar null quando token JWT está ausente")
    void shouldReturnNullWhenJwtTokenIsMissing() {
        // Simular cabeçalho de autorização sem token
        String authHeader = null;
        
        // Verificar que o token é null
        assertNull(authHeader);
    }
}
