package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    private CommentMapper commentMapper;
    private Comment comment;
    private CommentDto commentDto;
    private Item item;
    private User author;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        commentMapper = new CommentMapper();
        now = LocalDateTime.now();

        author = new User();
        author.setId(2L);
        author.setName("Дмитрий");

        item = new Item();
        item.setId(3L);

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Хороший инструмент");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Хороший инструмент");
    }

    @Test
    void toCommentDto_whenValidComment_thenReturnCorrectDto() {
        CommentDto result = commentMapper.toCommentDto(comment);

        assertNotNull(result);
        assertEquals(comment.getId(), result.getId());
        assertEquals(item.getId(), result.getItemId());
        assertEquals(comment.getText(), result.getText());
        assertEquals(author.getName(), result.getAuthorName());
        assertEquals(comment.getCreated(), result.getCreated());
    }

    @Test
    void toComment_whenValidArgs_thenReturnCorrectEntity() {
        Comment result = commentMapper.toComment(commentDto, item, author);

        assertNotNull(result);
        assertEquals(commentDto.getId(), result.getId());
        assertEquals(commentDto.getText(), result.getText());
        assertEquals(author, result.getAuthor());
        assertEquals(item, result.getItem());
    }
}
