package com.memorial.server.letter;

import com.memorial.server.letter.LetterDtos.CommentDto;
import com.memorial.server.letter.LetterDtos.HeartRequest;
import com.memorial.server.letter.LetterDtos.HeartResult;
import com.memorial.server.letter.LetterDtos.LetterDto;
import com.memorial.server.letter.LetterDtos.NewComment;
import com.memorial.server.letter.LetterDtos.NewLetter;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LetterController {

    private final LetterService service;

    public LetterController(LetterService service) {
        this.service = service;
    }

    @GetMapping("/api/spaces/{space}/letters")
    public List<LetterDto> list(@PathVariable String space) {
        return service.list(space);
    }

    @PostMapping("/api/spaces/{space}/letters")
    @ResponseStatus(HttpStatus.CREATED)
    public LetterDto write(@PathVariable String space, @RequestBody NewLetter req) {
        return service.write(space, req);
    }

    @PostMapping("/api/letters/{id}/hearts")
    public HeartResult heart(@PathVariable String id, @RequestBody HeartRequest req) {
        return service.heart(id, req.visitorId());
    }

    @GetMapping("/api/letters/{id}/comments")
    public List<CommentDto> comments(@PathVariable String id) {
        return service.comments(id);
    }

    @PostMapping("/api/letters/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto comment(@PathVariable String id, @RequestBody NewComment req) {
        return service.comment(id, req);
    }
}
