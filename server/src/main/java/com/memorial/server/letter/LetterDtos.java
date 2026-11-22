package com.memorial.server.letter;

import com.memorial.server.common.Access;
import java.time.Instant;

public final class LetterDtos {

    private LetterDtos() {}

    public record LetterDto(String id, String space, String to, String message, String name, int hearts, int comments, Instant createdAt, String owner) {
        static LetterDto of(Letter l) {
            return new LetterDto(l.getId(), l.getSpace(), l.getToName(), l.getMessage(), l.getAuthorName(), l.getHearts(),
                    l.getCommentCount(), l.getCreatedAt(), Access.tag(l.getOwnerHash()));
        }
    }

    public record CommentDto(String id, String letterId, String parentId, String name, String text, Instant createdAt, String owner) {
        static CommentDto of(Comment c) {
            return new CommentDto(c.getId(), c.getLetterId(), c.getParentId(), c.getAuthorName(), c.getText(), c.getCreatedAt(), Access.tag(c.getOwnerHash()));
        }
    }

    public record NewLetter(String to, String message, String name, String visitorId, String ownerKey) {}

    public record NewComment(String text, String name, String visitorId, String parentId, String ownerKey) {}

    public record HeartRequest(String visitorId) {}

    public record HeartResult(String letterId, int hearts, boolean added) {}
}
