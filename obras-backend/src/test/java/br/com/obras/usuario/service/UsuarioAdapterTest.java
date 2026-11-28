package br.com.obras.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import br.com.obras.shared.port.UsuarioResumo;
import br.com.obras.usuario.domain.Usuario;
import br.com.obras.usuario.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsuarioAdapterTest {

    private static final Long USUARIO_ID = 5L;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioAdapter adapter;

    @Test
    @DisplayName("Retorna o resumo de um usuário ativo")
    void retornaResumoDeUsuarioAtivo() {
        Usuario usuario = Usuario.cadastrar("Maria", "maria", "hash");
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

        Optional<UsuarioResumo> resumo = adapter.buscarResumo(USUARIO_ID);

        assertThat(resumo).isPresent();
        assertThat(resumo.get().nome()).isEqualTo("Maria");
        assertThat(resumo.get().estaAtivo()).isTrue();
    }

    @Test
    @DisplayName("Marca o resumo como inativo quando o usuário foi desativado")
    void marcaResumoComoInativoQuandoUsuarioDesativado() {
        Usuario usuario = Usuario.cadastrar("Maria", "maria", "hash");
        usuario.desativar();
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

        Optional<UsuarioResumo> resumo = adapter.buscarResumo(USUARIO_ID);

        assertThat(resumo).isPresent();
        assertThat(resumo.get().estaAtivo()).isFalse();
    }

    @Test
    @DisplayName("Retorna vazio quando o usuário não existe")
    void retornaVazioQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.empty());

        assertThat(adapter.buscarResumo(USUARIO_ID)).isEmpty();
    }
}