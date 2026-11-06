package edu.pucp.plg_back.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public String greeting(String payload) {
        // return "Hello, " + msg + "!";
        log.info("Received on /app/hello: {}", payload);
        return "Hello back, you said: " + payload;
    }
}
