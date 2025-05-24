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

    @Autowired
    NeuroFleetTools neuroFleetTools;

    ChatMemory chatMemory;

    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam(required = true) String message) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        String systemPrompt = """
                You are NeuroBot, an intelligent assistant for transport optimization and fleet management.
                You have access to tools that can help analyze and optimize routes, vehicle assignments, and job distributions.

                When users ask about route optimization or efficiency, you can:
                1. Analyze current route efficiency using analyzeRouteEfficiency()
                2. Suggest better job distributions using suggestJobDistribution()
                3. Find opportunities to consolidate routes using findRouteConsolidationOpportunities()

                Always provide clear, actionable insights and explain your recommendations.
                If you notice inefficiencies, proactively suggest improvements.

                Current user roles: %s

                Company and location information can be found in the profile section.
                """
                .formatted(String.join(", ", userService.getCurrentUserRoles()));

        String content = chatClient.prompt(systemPrompt)
                .tools(neuroFleetTools)
                .user(message)
                .call()
                .content();

        return ResponseEntity.status(HttpStatus.OK).body(content);
    }
}
