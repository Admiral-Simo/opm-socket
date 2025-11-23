package com.network.opmsocket.backend.chat.controller;

import com.network.opmsocket.backend.chat.repository.MessageRepository;
import com.network.opmsocket.backend.chat.model.Message;
import com.network.opmsocket.backend.chat.model.PublicMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final MessageRepository messageRepository;

    /**
     * Endpoint to retrieve the public chat history.
     * This is automatically secured by SecurityConfig to require an authenticated user.
     */
    @GetMapping("/public/history")
    public List<PublicMessageDto> getPublicChatHistory() {
        return messageRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * Helper method to convert a Message (Entity) to a PublicMessage (DTO).
     */
    private PublicMessageDto convertToDto(Message message) {
        return new PublicMessageDto(
                message.getSenderName(),
                message.getContent(),
                message.getTimestamp()
        );
    }
}