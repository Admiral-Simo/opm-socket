package com.network.opmsocket.backend.chat;

import com.network.opmsocket.backend.chat.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    public PublicMessage sendMessage(
            @Payload ChatMessage message,
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

        return new PublicMessage(
                savedMessage.getSenderName(),
                savedMessage.getContent(),
                savedMessage.getTimestamp()
        );
    }
}