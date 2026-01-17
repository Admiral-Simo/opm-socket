package com.network.opmsocket.backend.chat.controller;

import com.network.opmsocket.backend.chat.repository.AppUserRepository; // <--- Import this
import com.network.opmsocket.backend.chat.repository.MessageRepository;
import com.network.opmsocket.backend.chat.model.ChatMessageDto;
import com.network.opmsocket.backend.chat.model.Message;
import com.network.opmsocket.backend.chat.model.PublicMessageDto;
import com.network.opmsocket.backend.user.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final MessageRepository messageRepository;
    private final AppUserRepository appUserRepository;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public PublicMessageDto sendMessage(
            @Payload ChatMessageDto message,
            @AuthenticationPrincipal JwtAuthenticationToken principal) {

        Jwt jwt = principal.getToken();

        String userId = jwt.getSubject();
        String senderName = "Anonymous";

        Optional<AppUser> userOptional = appUserRepository.findById(userId);

        if (userOptional.isPresent()) {
            senderName = userOptional.get().getUsername();
        } else {
            String nameClaim = jwt.getClaimAsString("name");
            String usernameClaim = jwt.getClaimAsString("cognito:username");

            if (nameClaim != null) senderName = nameClaim;
            else if (usernameClaim != null) senderName = usernameClaim;
        }

        Message newMessage = new Message();
        newMessage.setSenderName(senderName);
        newMessage.setContent(message.getContent());

        Message savedMessage = messageRepository.save(newMessage);

        return new PublicMessageDto(
                savedMessage.getSenderName(),
                savedMessage.getContent(),
                savedMessage.getTimestamp()
        );
    }
}