package ch.zhaw.neurofleet.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.neurofleet.repository.JobRepository;
import ch.zhaw.neurofleet.service.CompanyService;
import ch.zhaw.neurofleet.service.UserService;
import ch.zhaw.neurofleet.tools.NeuroFleetTools;
import static ch.zhaw.neurofleet.security.Roles.*;

@RestController
@RequestMapping("/api")
public class ChatController {
    @Autowired
    JobRepository jobRepository;

    @Autowired
    CompanyService companyService;

    @Autowired
    UserService userService;

    @Autowired
    OpenAiChatModel chatModel;

    @Autowired
    ChatClient chatClient;

    ChatMemory chatMemory;

    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam(required = true) String message) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        String content = chatClient.prompt(
                "You are NeuroBot, an assistant for transport optimization and platform guidance.")
                .tools(new NeuroFleetTools()).user(message)
                .call()
                .content();

        return ResponseEntity.status(HttpStatus.OK).body(content);
    }
}
