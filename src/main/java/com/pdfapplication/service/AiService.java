package com.pdfapplication.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AiService {


    public Resource summarizePdf(MultipartFile file) throws IOException {
         String textData=file.getBytes().toString();
        return null;
    }

    public Resource translatePdf(MultipartFile file) {
    }

    public Resource generateQuestions(MultipartFile file) {
    }
}
