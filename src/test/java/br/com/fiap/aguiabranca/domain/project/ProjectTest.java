package br.com.fiap.aguiabranca.domain.project;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectTest {

    @Test
    @DisplayName("Deve criar projeto e atualizar progresso dentro do limite 0-100")
    void shouldCreateProjectAndUpdateProgress() {
        Project project = new Project("Hub de Inovação", 10, new BigDecimal("150000.00"));

        assertEquals("Hub de Inovação", project.getName());
        assertEquals(10, project.getProgress());
        assertEquals(new BigDecimal("150000.00"), project.getBudget());

        project.updateProgress(50);
        assertEquals(50, project.getProgress());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar progresso com valor menor que 0 ou maior que 100")
    void shouldThrowExceptionForInvalidProgress() {
        Project project = new Project("Hub de Inovação", 0, new BigDecimal("100000.00"));

        assertThrows(IllegalArgumentException.class, () -> project.updateProgress(-1));
        assertThrows(IllegalArgumentException.class, () -> project.updateProgress(101));
    }

    @Test
    void shouldDeriveStatusFromInitialProgress() {
        assertEquals(ProjectStatus.PLANNING, new Project("Novo", 0, BigDecimal.TEN).getStatus());
        assertEquals(ProjectStatus.IN_PROGRESS, new Project("Iniciado", 10, BigDecimal.TEN).getStatus());
        assertEquals(ProjectStatus.COMPLETED, new Project("Concluido", 100, BigDecimal.TEN).getStatus());
    }

    @Test
    void shouldKeepStatusConsistentWhenCorrectingProgress() {
        Project project = new Project("Projeto", 100, BigDecimal.TEN);
        project.updateProgress(70);
        assertEquals(ProjectStatus.IN_PROGRESS, project.getStatus());
        project.updateProgress(0);
        assertEquals(ProjectStatus.PLANNING, project.getStatus());
        project.updateProgress(100);
        assertEquals(ProjectStatus.COMPLETED, project.getStatus());
    }
}
