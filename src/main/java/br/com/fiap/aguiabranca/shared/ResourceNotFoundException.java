package br.com.fiap.aguiabranca.shared;

/** Recurso inexistente, ou existente e invisivel para quem pediu — vira 404. */
public class ResourceNotFoundException extends RuntimeException {

    private final String type;

    public ResourceNotFoundException(String type, String message) {
        super(message);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
