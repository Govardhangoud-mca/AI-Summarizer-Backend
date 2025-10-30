package com.summarizer_backend.service;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FileTextExtractor {

    public String extractText(MultipartFile file) throws IOException {
        String mimeType = file.getContentType();
        
        if (file.isEmpty() || mimeType == null) {
            throw new IOException("File content is empty or type cannot be determined.");
        }

        if (mimeType.equals("application/pdf")) {
            return extractTextFromPdf(file);
        } else if (mimeType.contains("vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return extractTextFromDocx(file);
        } else if (mimeType.equals("text/plain")) {
            return new String(file.getBytes());
        } else {
            throw new IOException("Unsupported file type: " + file.getOriginalFilename() + " (" + mimeType + ")");
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream());
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
}
