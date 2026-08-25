package br.com.fiap.aguiabranca.domain.idea;

import java.time.Instant;

public record IdeaResponse(
        Long id,
        String title,
        String description,
        Idea.Status status,
        Long ownerId,
        Instant createdAt,
        Instant reviewedAt) {

    public static IdeaResponse from(Idea idea) {
        return new IdeaResponse(
                idea.getId(),
                idea.getTitle(),
                idea.getDescription(),
                idea.getStatus(),
                idea.getOwner() == null ? null : idea.getOwner().getId(),
                idea.getCreatedAt(),
                idea.getReviewedAt());
    }
}
