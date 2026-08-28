package br.com.fiap.aguiabranca.domain.strategy;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Assimetria proposital: leitura liberada para os tres perfis, escrita so para GESTOR e
 * LIDERANCA. E o unico recurso do hub com essa regra — a #17 existe para que ela nao se perca
 * numa refatoracao do SecurityConfig.
 */
@RestController
@RequestMapping("/strategies")
public class StrategyController {

    private final StrategyService strategyService;

    public StrategyController(StrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @GetMapping
    public List<StrategyResponse> list() {
        return strategyService.list().stream().map(StrategyResponse::from).toList();
    }

    @GetMapping("/{id}")
    public StrategyResponse findById(@PathVariable Long id) {
        return StrategyResponse.from(strategyService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR', 'LIDERANCA')")
    public ResponseEntity<StrategyResponse> create(@Valid @RequestBody StrategyRequest request) {
        Strategy strategy = strategyService.create(request);
        return ResponseEntity
                .created(URI.create("/strategies/" + strategy.getId()))
                .body(StrategyResponse.from(strategy));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR', 'LIDERANCA')")
    public StrategyResponse update(@PathVariable Long id, @Valid @RequestBody StrategyRequest request) {
        return StrategyResponse.from(strategyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR', 'LIDERANCA')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        strategyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
