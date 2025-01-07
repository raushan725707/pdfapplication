package com.pdfapplication.controller;

import com.pdfapplication.config.PdfConfig;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
	
	 @PostMapping("/convert")
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
	    
	    

	
	
}
