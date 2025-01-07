package com.pdfapplication.controller;

import com.pdfapplication.config.PdfConfig;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/pdf")
public class OptimizePdfController {


    @PostMapping("/compress/high")
    public ResponseEntity<?> compressHigh(@RequestParam("file") MultipartFile file) throws IOException {
        return compressPdf(file, CompressionLevel.HIGH);
    }

    @PostMapping("/compress/recommended")
    public ResponseEntity<?> compressRecommended(@RequestParam("file") MultipartFile file) throws IOException {
        return compressPdf(file, CompressionLevel.RECOMMENDED);
    }

    @PostMapping("/compress/low")
    public ResponseEntity<?> compressLow(@RequestParam("file") MultipartFile file) throws IOException {
        return compressPdf(file, CompressionLevel.LOW);
    }

    private ResponseEntity<?> compressPdf(MultipartFile file, CompressionLevel level) throws IOException {
        byte[] pdfBytes = file.getBytes();
      File f=PdfConfig.convertMultipartFileToFile(file);
        try (PDDocument originalDocument = Loader.loadPDF(f);
             PDDocument compressedDocument = new PDDocument()) {

            PDFRenderer pdfRenderer = new PDFRenderer(originalDocument);
            PDRectangle pageSize = originalDocument.getPage(0).getMediaBox();

            for (int page = 0; page < originalDocument.getNumberOfPages(); ++page) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, getDpiForLevel(level), ImageType.RGB);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", baos);

                PDPage pdPage = new PDPage(pageSize);
                compressedDocument.addPage(pdPage);
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(compressedDocument, baos.toByteArray(), "compressed");
                try (PDPageContentStream contentStream = new PDPageContentStream(compressedDocument, pdPage)) {
                    contentStream.drawImage(pdImage, 0, 0, pageSize.getWidth(), pageSize.getHeight());
                }
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            compressedDocument.save(byteArrayOutputStream);

            byte[] content = byteArrayOutputStream.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("compressed.pdf")
                    .build());
            headers.setContentType(MediaType.APPLICATION_PDF);
            System.out.println("size "+content.length);
            double sizeInKb = content.length / 1024.0;
            System.out.println("Size: " + sizeInKb + " KB");
            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        }
    }

    private float getDpiForLevel(CompressionLevel level) {
        switch (level) {
            case HIGH:
                return 80f;
            case RECOMMENDED:
                return 50;
            case LOW:
                return 40;
            default:
                return 50;
        }
    }

    private float getCompressionQuality(CompressionLevel level) {
        switch (level) {
            case HIGH:
                return 0.3f;
            case RECOMMENDED:
                return 0.2f;
            case LOW:
                return 0.1f;
            default:
                return 0.2f;
        }




}
    public enum CompressionLevel {
        HIGH,
        RECOMMENDED,
        LOW
    }


}
