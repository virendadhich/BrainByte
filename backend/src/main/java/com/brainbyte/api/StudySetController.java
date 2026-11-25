package com.brainbyte.api;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-sets")
public class StudySetController {
    private final AuthService authService;
    private final StudySetService studySetService;
    private final GeminiService geminiService;

    public StudySetController(
            AuthService authService,
            StudySetService studySetService,
            GeminiService geminiService) {
        this.authService = authService;
        this.studySetService = studySetService;
        this.geminiService = geminiService;
    }

    @GetMapping
    public Map<String, List<StudySet>> list(HttpServletRequest request) {
        Account account = requireAccount(request);
        return Map.of("studySets", studySetService.list(account.id()));
    }

    @PostMapping
    public ResponseEntity<Map<String, StudySet>> save(
            @RequestBody StudySet set,
            HttpServletRequest request) {
        Account account = requireAccount(request);
        StudySet saved = studySetService.save(account.id(), set);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("studySet", saved));
    }

    @PostMapping("/generate")
    public Map<String, StudySet> generate(
            @RequestBody GenerateRequest body,
            HttpServletRequest request) {
        Account account = requireAccount(request);
        return Map.of("studySet", geminiService.generate(body.text(), body.title(), account.id()));
    }

    private Account requireAccount(HttpServletRequest request) {
        return authService.currentAccount(AuthController.getSessionToken(request))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please sign in to continue."));
    }

    public record GenerateRequest(String text, String title) {}
}
