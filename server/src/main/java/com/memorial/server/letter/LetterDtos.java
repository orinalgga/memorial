package com.memorial.server.letter;

import java.time.Instant;

public final class LetterDtos {

    private LetterDtos() {}

    public record LetterDto(String id, String space, String to, String message, String name, int hearts, int comments, Instant createdAt) {
        static LetterDto of(Letter l) {
            return new LetterDto(l.getId(), l.getSpace(), l.getToName(), l.getMessage(), l.getAuthorName(), l.getHearts(),
                    l.getCommentCount(), l.getCreatedAt());
        }
    }

    public record CommentDto(String id, String letterId, String parentId, String name, String text, Instant createdAt) {
        static CommentDto of(Comment c) {
            return new CommentDto(c.getId(), c.getLetterId(), c.getParentId(), c.getAuthorName(), c.getText(), c.getCreatedAt());
        }
    }

    public record NewLetter(String to, String message, String name, String visitorId) {}

    public record NewComment(String text, String name, String visitorId, String parentId) {}

    public record HeartRequest(String visitorId) {}

    public record HeartResult(String letterId, int hearts, boolean added) {}
}
