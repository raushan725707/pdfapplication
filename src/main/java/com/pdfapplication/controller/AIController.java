package com.pdfapplication.controller;

import com.pdfapplication.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AIController {


    @Autowired
   private AiService aiService;


    ResponseEntity<?> pdfSummarizer(@RequestParam MultipartFile file){
      Resource resource=  aiService.summarizePdf(file);
        return ResponseEntity.ok(resource);
    }

    ResponseEntity<?> translatePdf(@RequestParam MultipartFile file){
        aiService.translatePdf(file);
    }

    ResponseEntity<?> generateQuestions(@RequestParam MultipartFile file){
        aiService.generateQuestions(file);
    }


}
