package br.com.obras.diario.repository;

import br.com.obras.diario.domain.Paralisacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParalisacaoRepository extends JpaRepository<Paralisacao, Long> {}
