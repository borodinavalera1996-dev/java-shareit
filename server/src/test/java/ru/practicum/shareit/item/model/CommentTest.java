package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    @Test
    void testCommentGettersAndSetters() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Отличная дрель!");

        Item item = new Item();
        item.setId(2L);
        comment.setItem(item);

        User author = new User();
        author.setId(3L);
        comment.setAuthor(author);

        LocalDateTime now = LocalDateTime.now();
        comment.setCreated(now);

        assertEquals(1L, comment.getId());
        assertEquals("Отличная дрель!", comment.getText());
        assertEquals(2L, comment.getItem().getId());
        assertEquals(3L, comment.getAuthor().getId());
        assertEquals(now, comment.getCreated());
        assertNotNull(comment.toString());
    }
}
