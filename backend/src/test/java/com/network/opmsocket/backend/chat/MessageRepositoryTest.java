package com.network.opmsocket.backend.chat;

import com.network.opmsocket.backend.chat.model.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Test
    public void shouldSaveAndFetchMessages() {
        // 1. Arrange
        Message msg = new Message();
        msg.setSenderName("Alice");
        msg.setContent("Test DB Message");

        // 2. Act
        messageRepository.save(msg);
        List<Message> foundMessages = messageRepository.findAll();

        // 3. Assert
        assertThat(foundMessages).hasSize(1);
        assertThat(foundMessages.get(0).getSenderName()).isEqualTo("Alice");
        assertThat(foundMessages.get(0).getContent()).isEqualTo("Test DB Message");
        assertThat(foundMessages.get(0).getTimestamp()).isNotNull();
    }
}