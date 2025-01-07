package com.pdfapplication.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ConvertToPdfService {

	public byte[] convertImagesToPdf(List<byte[]> imageBytesList, String orientation, String pageSizeType) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (PDDocument document = new PDDocument()) {
			PDRectangle pageSize = getPageSize(pageSizeType, orientation);

			for (byte[] imageBytes : imageBytesList) {
				PDPage page = new PDPage(pageSize);
				document.addPage(page);
				PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageBytes, null);

				try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
					float scale = Math.min(pageSize.getWidth() / pdImage.getWidth(), pageSize.getHeight() / pdImage.getHeight());
					float x = (pageSize.getWidth() - (pdImage.getWidth() * scale)) / 2;
					float y = (pageSize.getHeight() - (pdImage.getHeight() * scale)) / 2;
					contentStream.drawImage(pdImage, x, y, pdImage.getWidth() * scale, pdImage.getHeight() * scale);
				}
			}

			document.save(outputStream);
		}

		return outputStream.toByteArray();
	}

	private PDRectangle getPageSize(String pageSizeType, String orientation) {
		PDRectangle pageSize;
		switch (pageSizeType.toUpperCase()) {
			case "A4":
				pageSize = PDRectangle.A4;
				break;
			case "USLETTER":
				pageSize = PDRectangle.LETTER;
				break;
			case "SAME_SIZE":
				pageSize = PDRectangle.LETTER; // Assuming LETTER as default, since PDFBox doesn't have a DEFAULT page size
				break;
			default:
				pageSize = PDRectangle.A4; // Default to A4 if not specified
				break;
		}

		if ("LANDSCAPE".equalsIgnoreCase(orientation)) {
			pageSize = new PDRectangle(pageSize.getHeight(), pageSize.getWidth());
		}

		return pageSize;
	}
}
