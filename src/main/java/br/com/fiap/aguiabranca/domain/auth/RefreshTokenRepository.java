package br.com.fiap.aguiabranca.domain.auth;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHashForUpdate(String hash);

    List<RefreshToken> findByFamilyId(UUID familyId);

    void deleteAll();
}
