package com.pdfapplication.service;


import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFColor;

import com.itextpdf.text.Document;
import org.apache.poi.ss.usermodel.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
;
import java.util.Iterator;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

	public byte[] convertJpgToPdf(MultipartFile file) throws IOException {
		// Create a new PDF document
		PDDocument document = new PDDocument();

		// Create a page for the PDF
		PDPage page = new PDPage();
		document.addPage(page);

		// Load the image as a PDF object using the file's byte array
		PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, file.getBytes(), file.getOriginalFilename());

		// Create content stream to write image to PDF
		PDPageContentStream contentStream = new PDPageContentStream(document, page);

		// Get image dimensions and position
		float x = 100; // x-position
		float y = 100; // y-position
		float width = pdImage.getWidth();
		float height = pdImage.getHeight();

		// Draw image on the page
		contentStream.drawImage(pdImage, x, y, width, height);

		// Close the content stream
		contentStream.close();

		// Save the PDF to a byte array
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		document.save(byteArrayOutputStream);
		document.close();

		// Return the PDF as a byte array
		return byteArrayOutputStream.toByteArray();
	}

	public List<byte[]> convertWordToPdf(List<MultipartFile> files) throws IOException, Docx4JException {
		List<byte[]> pdfList = new ArrayList<>();

		for (MultipartFile file : files) {
			// Process each uploaded Word file (Word format: .docx)
			if (file.getOriginalFilename().endsWith(".docx")) {
				pdfList.add(convertSingleWordToPdf(file));
			} else {
				throw new IOException("Unsupported file type: " + file.getOriginalFilename());
			}
		}

		return pdfList;
	}

	public byte[] convertSingleWordToPdf(MultipartFile file) throws IOException, Docx4JException {
		// Load the Word document (.docx)
		InputStream inputStream = file.getInputStream();
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inputStream);

		// Create a ByteArrayOutputStream to hold the generated PDF
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		// Convert the Word document to PDF using XSLFOPdfConversion
	//	PdfConversion converter = new XSLFOPdfConversion(wordMLPackage);
		//converter.convert(byteArrayOutputStream, null);  // Pass a null file for no output to disk

		// Return the PDF byte array
		return byteArrayOutputStream.toByteArray();
	}

	public byte[] convertExcelToPdf(MultipartFile file) throws IOException, DocumentException {
		// Load the Excel file using Apache POI
		Workbook workbook = new XSSFWorkbook(file.getInputStream());
		ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

		// Create a PDF document using iText
		PdfDocument pdfDocument = new PdfDocument();
		Document document = new Document();

		// Iterate over each sheet in the Excel file
		for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			Iterator<Row> rowIterator = sheet.iterator();

			// Create a table in the PDF to hold Excel data
			Table pdfTable = new Table(sheet.getRow(0).getPhysicalNumberOfCells());

			// Iterate over each row in the sheet
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Iterator<org.apache.poi.ss.usermodel.Cell> cellIterator = row.iterator();

				// Iterate over each cell in the row and convert it to iText Cell
				while (cellIterator.hasNext()) {
					Cell cell = (Cell) cellIterator.next();
					String cellText = cell.toString(); // Convert POI Cell to String

					// Create an iText Cell and add it to the Table
					pdfTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(cellText)));
				}
			}

			// Add the table to the PDF document
			document.add((Element) pdfTable);
		}

		// Close the document and return the PDF as a byte array
		document.close();
		return pdfOutputStream.toByteArray();
	}

















	public byte[] convertExcelToPdf2(MultipartFile file) throws IOException, DocumentException {
		Workbook workbook = new XSSFWorkbook(file.getInputStream());  // Read the Excel file
		ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

		// Create a PDF document using iText
		Document document = new Document();
		PdfWriter.getInstance(document, pdfOutputStream);
		document.open();

		// Iterate over all sheets in the Excel file
		for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			PdfPTable table = new PdfPTable(sheet.getRow(0).getPhysicalNumberOfCells());

			// Iterate over rows in the sheet
			for (Row row : sheet) {
				// Skip the header row if necessary
				if (row.getRowNum() == 0) continue;

				// Iterate over cells in each row
				for (Cell cell : row) {
					String cellValue = getCellValue(cell);
					PdfPCell pdfCell = new PdfPCell(new Phrase(cellValue));

					// Apply styling like font, background color, alignment, etc.
					applyCellStyle(cell, pdfCell);

					// Add the cell to the table
					table.addCell(pdfCell);
				}
			}

			// Add the table to the document
			document.add(table);
		}

		// Close the document
		document.close();
		workbook.close();

		return pdfOutputStream.toByteArray();  // Return the generated PDF as a byte array
	}

	// Get the cell value from Apache POI Cell
	private String getCellValue(Cell cell) {
		String cellValue = "";
		switch (cell.getCellType()) {
			case STRING:
				cellValue = cell.getStringCellValue();
				break;
			case NUMERIC:
				cellValue = String.valueOf(BigDecimal.valueOf(cell.getNumericCellValue()));
				break;
			case BLANK:
				break;
			default:
				cellValue = "";
		}
		return cellValue;
	}

	// Apply cell style (font, background, and alignment)
	private void applyCellStyle(Cell cell, PdfPCell pdfCell) {
		CellStyle cellStyle = cell.getCellStyle();

		// Font styling
		Font font = getCellFont(cell);
		pdfCell.setPhrase(new Phrase(pdfCell.getPhrase().getContent(), font));

		// Background color
		setCellBackgroundColor(cell, pdfCell);

		// Alignment
		setCellAlignment(cell, pdfCell);
	}

	// Get the font styling from Apache POI
	private Font getCellFont(Cell cell) {
		Font font = new Font();
		CellStyle cellStyle = cell.getCellStyle();
		org.apache.poi.ss.usermodel.Font cellFont = cell.getSheet()
				.getWorkbook()
				.getFontAt(cellStyle.getFontIndexAsInt());

		if (cellFont.getItalic()) {
			font.setStyle(Font.ITALIC);
		}
		if (cellFont.getStrikeout()) {
			font.setStyle(Font.STRIKETHRU);
		}
		if (cellFont.getUnderline() == 1) {
			font.setStyle(Font.UNDERLINE);
		}
		font.setSize(cellFont.getFontHeightInPoints());

		if (cellFont.getBold()) {
			font.setStyle(Font.BOLD);
		}

		String fontName = cellFont.getFontName();
		font.setFamily(fontName);

		return font;
	}

	// Set the background color for the PdfPCell
	private void setCellBackgroundColor(Cell cell, PdfPCell pdfCell) {
		short bgColorIndex = cell.getCellStyle().getFillForegroundColor();
		if (bgColorIndex != IndexedColors.AUTOMATIC.getIndex()) {
			XSSFColor bgColor = (XSSFColor) cell.getCellStyle().getFillForegroundColorColor();
			if (bgColor != null) {
				byte[] rgb = bgColor.getRGB();
				if (rgb != null && rgb.length == 3) {
					pdfCell.setBackgroundColor(new BaseColor(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF));
				}
			}
		}




	}
	void setCellAlignment(Cell cell, PdfPCell cellPdf) {
		CellStyle cellStyle = cell.getCellStyle();

		HorizontalAlignment horizontalAlignment = cellStyle.getAlignment();

		switch (horizontalAlignment) {
			case LEFT:
				cellPdf.setHorizontalAlignment(Element.ALIGN_LEFT);
				break;
			case CENTER:
				cellPdf.setHorizontalAlignment(Element.ALIGN_CENTER);
				break;
			case JUSTIFY:
			case FILL:
				cellPdf.setVerticalAlignment(Element.ALIGN_JUSTIFIED);
				break;
			case RIGHT:
				cellPdf.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
		}
	}



}