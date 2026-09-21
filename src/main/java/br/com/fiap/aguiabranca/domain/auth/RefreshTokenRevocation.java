package br.com.fiap.aguiabranca.domain.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * A revogacao que o SELECT ... FOR UPDATE garantia, agora sem lock.
 *
 * O Mongo nao tem lock pessimista de linha. O que substitui e o findAndModify: a condicao
 * "ainda nao revogado" e a escrita acontecem numa operacao atomica no servidor, entao de duas
 * requisicoes simultaneas com o mesmo refresh apenas uma recebe o documento. A outra recebe
 * vazio e cai no caminho de reuso, que derruba a familia inteira — exatamente o comportamento
 * de antes.
 */
public interface RefreshTokenRevocation {

    /** Marca como revogado se ainda estiver ativo, devolvendo o documento ja revogado. */
    Optional<RefreshToken> claimIfActive(String tokenHash, Instant now);

    /** Revoga de uma vez todos os tokens ainda ativos da familia (sessao comprometida). */
    void revokeFamily(UUID familyId, Instant now);
}
