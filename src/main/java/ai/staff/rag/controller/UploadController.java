package ai.staff.rag.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import ai.staff.rag.service.RagService;

@RestController
@RequiredArgsConstructor
public class UploadController {

	@Autowired
    private RagService ragService;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("File empty bro");
        ragService.ingestPdf(file);
        return ResponseEntity.ok("Ingested " + file.getOriginalFilename() + ". Ready to chat.");
    }

    @PostMapping("/chat/ask")
    public String ask(@RequestBody String question) {
        return ragService.ask(question); // Old /chat/ask replace chey idi tho
    }
}