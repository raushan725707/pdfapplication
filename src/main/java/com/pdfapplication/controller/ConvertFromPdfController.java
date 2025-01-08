package com.pdfapplication.controller;

import com.itextpdf.text.pdf.PdfReader;
import com.pdfapplication.config.PdfConfig;
import com.pdfapplication.service.ConvertFromPdfService;
import org.apache.pdfbox.Loader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api")
public class ConvertFromPdfController {

    @Autowired
    private ConvertFromPdfService convertFromPdfService;

    @PostMapping("/pdftojpg")
    public ResponseEntity<ByteArrayResource> convertPdfToJpg(@RequestParam("file") MultipartFile file) throws IOException {
        // Convert the PDF file to JPG images
        byte[] zipFileBytes = convertPdfToImagesAndZip(file);

        // Return the ZIP file containing all JPG images as a ByteArrayResource
        ByteArrayResource byteArrayResource = new ByteArrayResource(zipFileBytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted-images.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM) // For ZIP file
                .body(byteArrayResource);
    }


    public byte[] convertPdfToImagesAndZip(MultipartFile filee) throws IOException {
        // Convert the MultipartFile to InputStream
         InputStream inputStream = filee.getInputStream();
        File f= PdfConfig.convertMultipartFileToFile(filee);
        // Use iText's PdfReader to read the PDF
        PdfReader reader = new PdfReader(inputStream);

        // Create a ByteArrayOutputStream to hold the ZIP file
        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(zipOutputStream)) {
            int numberOfPages = reader.getNumberOfPages();

            // Load the PDF using PDFBox
            PDDocument document = Loader.loadPDF(f);// This is where we load the PDF

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            // Loop through all pages in the PDF and create a JPG image for each
            for (int pageIndex = 0; pageIndex < numberOfPages; pageIndex++) {
                // Render the PDF page to a BufferedImage
                BufferedImage bufferedImage = pdfRenderer.renderImage(pageIndex);

                // Create a zip entry for each image
                ZipEntry zipEntry = new ZipEntry("page_" + (pageIndex + 1) + ".jpg");
                zos.putNextEntry(zipEntry);

                // Convert the BufferedImage to JPG and write to the zip output stream
                ImageIO.write(bufferedImage, "JPEG", zos);

                // Close the current zip entry
                zos.closeEntry();
            }
            // Close the PDF document
            document.close();
        }
        return zipOutputStream.toByteArray();
    }




    //    private byte[] convertPdfToImagesAndZip(MultipartFile file) throws IOException {
//        // Convert the MultipartFile to InputStream
//
//        File f= PdfConfig.convertMultipartFileToFile(file);
//        // Load the PDF document using Apache PDFBox
//        PDDocument document = PDFDocument.load(f);
//
////        PDDocument document = Loader.loadPDF(f);
//        PDFRenderer pdfRenderer = new PDFRenderer(document);
//
//        // Create a ByteArrayOutputStream to hold the ZIP file
//        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
//        try (ZipOutputStream zos = new ZipOutputStream(zipOutputStream)) {
//            int numberOfPages = document.getNumberOfPages();
//
//            // Loop through all pages in the PDF and create a JPG image for each
//            for (int pageIndex = 0; pageIndex < numberOfPages; pageIndex++) {
//                BufferedImage bufferedImage = pdfRenderer.renderImage(pageIndex);
//
//                // Create a zip entry for each image
//                ZipEntry zipEntry = new ZipEntry("page_" + (pageIndex + 1) + ".jpg");
//                zos.putNextEntry(zipEntry);
//
//                // Convert the BufferedImage to JPG and write to the zip output stream
//                ImageIO.write(bufferedImage, "JPEG", zos);
//
//                // Close the current zip entry
//                zos.closeEntry();
//            }
//        }
//
//        // Close the PDF document
//        document.close();
//
//        return zipOutputStream.toByteArray();
//    }

}
