package com.memorial.server.letter;

import com.memorial.server.common.ApiException;
import com.memorial.server.common.Ids;
import com.memorial.server.common.Spaces;
import com.memorial.server.common.Text;
import com.memorial.server.letter.LetterDtos.CommentDto;
import com.memorial.server.letter.LetterDtos.HeartResult;
import com.memorial.server.letter.LetterDtos.LetterDto;
import com.memorial.server.letter.LetterDtos.NewComment;
import com.memorial.server.letter.LetterDtos.NewLetter;
import com.memorial.server.live.Events;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LetterService {

    private final LetterRepository letters;
    private final LetterHeartRepository hearts;
    private final CommentRepository comments;
    private final Spaces spaces;
    private final Events events;

    public LetterService(LetterRepository letters, LetterHeartRepository hearts, CommentRepository comments, Spaces spaces, Events events) {
        this.letters = letters;
        this.hearts = hearts;
        this.comments = comments;
        this.spaces = spaces;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<LetterDto> list(String space) {
        spaces.require(space);
        return letters.findBySpaceOrderByCreatedAtDesc(space, PageRequest.of(0, 200)).stream().map(LetterDto::of).toList();
    }

    @Transactional
    public LetterDto write(String space, NewLetter req) {
        spaces.require(space);
        Letter l = new Letter(Ids.newId(), space, Text.required(req.to(), 30, "받는 분"), Text.required(req.message(), 600, "전하고 싶은 말"),
                Text.name(req.name()), visitor(req.visitorId()), Instant.now());
        letters.save(l);
        LetterDto dto = LetterDto.of(l);
        events.space(space, "letter", dto);
        return dto;
    }

    /** 같은 방문자의 두 번째 공감은 수를 올리지 않는다. */
    @Transactional
    public HeartResult heart(String letterId, String visitorId) {
        Letter l = letter(letterId);
        String v = visitor(visitorId);
        if (v == null) throw ApiException.badRequest("방문자 정보가 없어요. 페이지를 새로 고쳐 주세요.");
        if (hearts.existsById(new LetterHeart.Key(letterId, v))) return new HeartResult(letterId, l.getHearts(), false);
        hearts.save(new LetterHeart(letterId, v, Instant.now()));
        letters.addHeart(letterId);
        int count = l.getHearts() + 1;
        events.space(l.getSpace(), "heart", Map.of("letterId", letterId, "hearts", count));
        return new HeartResult(letterId, count, true);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> comments(String letterId) {
        letter(letterId);
        return comments.findByLetterIdOrderByCreatedAtAsc(letterId, PageRequest.of(0, 500)).stream().map(CommentDto::of).toList();
    }

    @Transactional
    public CommentDto comment(String letterId, NewComment req) {
        Letter l = letter(letterId);
        String parent = null;
        if (req.parentId() != null && !req.parentId().isBlank()) {
            Comment p = comments.findById(req.parentId())
                    .filter(c -> c.getLetterId().equals(letterId))
                    .orElseThrow(() -> ApiException.badRequest("답글을 달 댓글을 찾을 수 없어요."));
            // 답글은 한 단계까지만 둔다. 답글에 단 답글은 원래 댓글 아래로 모은다.
            parent = p.getParentId() != null ? p.getParentId() : p.getId();
        }
        Comment c = new Comment(Ids.newId(), letterId, parent, Text.name(req.name()), visitor(req.visitorId()),
                Text.required(req.text(), 200, "댓글"), Instant.now());
        comments.save(c);
        letters.addComment(letterId);
        CommentDto dto = CommentDto.of(c);
        events.space(l.getSpace(), "comment", Map.of("comment", dto, "comments", l.getCommentCount() + 1));
        return dto;
    }

    private Letter letter(String id) {
        return letters.findById(id).orElseThrow(() -> ApiException.notFound("찾을 수 없는 사연이에요."));
    }

    private static String visitor(String v) {
        return Ids.isVisitorId(v) ? v : null;
    }
}
