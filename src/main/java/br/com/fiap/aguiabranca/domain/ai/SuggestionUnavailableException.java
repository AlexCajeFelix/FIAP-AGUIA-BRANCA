package br.com.fiap.aguiabranca.domain.ai;

/** O Gemini nao respondeu a tempo, recusou, ou devolveu algo que nao da para aproveitar. */
public class SuggestionUnavailableException extends RuntimeException {

    public SuggestionUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public SuggestionUnavailableException(String message) {
        super(message);
    }
}
