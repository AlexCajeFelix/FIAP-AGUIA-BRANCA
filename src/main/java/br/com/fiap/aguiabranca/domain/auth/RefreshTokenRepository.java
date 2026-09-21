package br.com.fiap.aguiabranca.domain.auth;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, Long>, RefreshTokenRevocation {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByFamilyId(UUID familyId);
}
