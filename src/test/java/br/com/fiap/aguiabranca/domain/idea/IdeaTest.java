package br.com.fiap.aguiabranca.domain.idea;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdeaTest {

    @Test
    @DisplayName("Deve criar uma ideia com status inicial DRAFT")
    void shouldCreateIdeaWithDraftStatus() {
        Idea idea = new Idea("Sistema de Rotas", "Otimização de logística de frotas");

        assertEquals("Sistema de Rotas", idea.getTitle());
        assertEquals("Otimização de logística de frotas", idea.getDescription());
        assertEquals(Idea.Status.DRAFT, idea.getStatus());
    }

    @Test
    @DisplayName("Deve alterar o status ao revisar a ideia")
    void shouldUpdateStatusOnReview() {
        Idea idea = new Idea("Portal do Cliente", "Autoatendimento para clientes");
        idea.review(Idea.Status.APPROVED);

        assertEquals(Idea.Status.APPROVED, idea.getStatus());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar revisar para status DRAFT ou nulo")
    void shouldThrowExceptionForInvalidReviewStatus() {
        Idea idea = new Idea("Nova Frota", "Renovação de veículos");

        assertThrows(IllegalArgumentException.class, () -> idea.review(null));
        assertThrows(IllegalArgumentException.class, () -> idea.review(Idea.Status.DRAFT));
    }
}
