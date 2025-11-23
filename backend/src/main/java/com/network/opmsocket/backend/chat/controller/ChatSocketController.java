package com.network.opmsocket.backend.chat.controller;

import com.network.opmsocket.backend.chat.repository.MessageRepository;
import com.network.opmsocket.backend.chat.model.ChatMessageDto;
import com.network.opmsocket.backend.chat.model.Message;
import com.network.opmsocket.backend.chat.model.PublicMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final MessageRepository messageRepository;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public PublicMessageDto sendMessage(
            @Payload ChatMessageDto message,
            @AuthenticationPrincipal JwtAuthenticationToken principal) {

        Jwt jwt = principal.getToken();
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null) {
            username = "Anonymous";
        }

        Message newMessage = new Message();
        newMessage.setSenderName(username);
        newMessage.setContent(message.getContent());

        Message savedMessage = messageRepository.save(newMessage);

        return new PublicMessageDto(
                savedMessage.getSenderName(),
                savedMessage.getContent(),
                savedMessage.getTimestamp()
        );
    }
}