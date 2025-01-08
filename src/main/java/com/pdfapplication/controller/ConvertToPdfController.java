package com.pdfapplication.controller;

import com.itextpdf.text.DocumentException;
import com.pdfapplication.config.PdfConfig;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;




import com.pdfapplication.service.ConvertToPdfService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class ConvertToPdfController {







private final	ConvertToPdfService convertToPdfService;
	
	public ConvertToPdfController(ConvertToPdfService convertToPdfService){
		this.convertToPdfService=convertToPdfService;

		}
	
	 @PostMapping("/convertJpgToPdf")
	    public ResponseEntity<byte[]> convertMultipleJpgToPdf(
	            @RequestParam("files")  MultipartFile[] files,
	            @RequestParam("orientation") String orientation,
	            @RequestParam("pageSize") String pageSize) throws IOException {

	        List<byte[]> imageBytesList = PdfConfig.convertFilesToBytes(files);
	        byte[] pdfBytes = convertToPdfService.convertImagesToPdf(imageBytesList, orientation, pageSize);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_PDF);
	        headers.setContentDispositionFormData("attachment", "images.pdf");

	        return ResponseEntity.ok()
	                .headers(headers)
	                .body(pdfBytes);
	    }
	    
	    //jpg to pdf
		@PostMapping("/convertJpgToPdff")
		public ResponseEntity<byte[]> convertJpgToPdf(@RequestParam MultipartFile file) {
			try {
				byte[] pdfContent = convertToPdfService.convertJpgToPdf(file);

				// Set headers for PDF download
				HttpHeaders headers = new HttpHeaders();
				headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted_image.pdf");
				headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

				// Return the PDF as a byte array
				return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
			} catch (IOException e) {
				e.printStackTrace();
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}




	@PostMapping("/convertwordtopdf")
	public ResponseEntity<List<byte[]>> convertWordToPdf(@RequestParam("files") List<MultipartFile> files) {
		try {
			// Convert multiple Word files to PDF
			List<byte[]> pdfFiles = convertToPdfService.convertWordToPdf(files);

			// Set headers for PDF download (if needed for returning multiple PDFs)
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted_files.zip");
			headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

			// You may want to compress all PDF files into a ZIP before sending them
			return new ResponseEntity<>(pdfFiles, headers, HttpStatus.OK);

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Docx4JException e) {
            throw new RuntimeException(e);
        }
    }


	@PostMapping("/convertexceltopdf")
	public ResponseEntity<List<byte[]>> convertExcelFilesToPdf(@RequestParam("files") MultipartFile[] files) {
		List<byte[]> pdfs = new ArrayList<>();

		try {
			for (MultipartFile file : files) {
				byte[] pdfBytes = convertToPdfService.convertExcelToPdf(file);
				pdfs.add(pdfBytes);  // Add each converted PDF byte array to the list
			}
			return ResponseEntity.ok(pdfs);  // Return the list of PDFs
		} catch (IOException | DocumentException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(null);  // Return an error response if conversion fails
		}
	}
	@PostMapping("/convertexceltopdf2")
	public ResponseEntity<byte[]> convertExcelToPdf(@RequestParam("file") MultipartFile file) throws IOException, DocumentException {
		byte[] pdfBytes = convertToPdfService.convertExcelToPdf2(file);


		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("attachment", "images.pdf");

		return ResponseEntity.ok()
				.headers(headers)
				.body(pdfBytes);


//		HttpHeaders headers = new HttpHeaders();
//		headers.add("Content-Disposition", "attachment; filename=converted.pdf");
//
//		return ResponseEntity.ok()
//				.headers(headers)
//				.body(pdfBytes);
	}

	/*
	Extracting structured data from images using AI in Java
	 */
	
	
}