package br.com.fiap.aguiabranca.domain.idea;

public interface IdeaReviewStore {
    boolean saveReviewIfPending(Idea reviewed);
}
