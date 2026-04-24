package com.skillforge.app.controller;

import com.skillforge.app.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<?> askChatbot(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        Object userId = request.get("userId");
        Object context = request.get("context");

        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Message cannot be empty");
        }

        Long uid = userId != null ? Long.parseLong(userId.toString()) : 0L;

        try {
            String reply = chatService.respond(uid, message, context);
            Map<String, String> response = new HashMap<>();
            response.put("reply", reply);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("reply", "I'm having trouble at the moment. Please try again!");
            return ResponseEntity.status(500).body(err);
        }
    }
}
