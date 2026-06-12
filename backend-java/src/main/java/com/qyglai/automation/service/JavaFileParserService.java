package com.qyglai.automation.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.ParsedFileResult;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Java 文件文本解析服务。
 */
@Service
public class JavaFileParserService {

    private final ObjectMapper objectMapper;
    private final JavaOcrService ocrService;

    public JavaFileParserService(ObjectMapper objectMapper, JavaOcrService ocrService) {
        this.objectMapper = objectMapper;
        this.ocrService = ocrService;
    }

    public ParsedFileResult parse(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename() == null ? "upload.bin" : file.getOriginalFilename();
            String extension = extension(filename);
            byte[] bytes = file.getBytes();
            List<String> warnings = new ArrayList<>();
            String ocrEngine = null;
            int pageCount = 1;
            String rawText = switch (extension) {
                case "txt", "md", "log", "csv" -> decode(file.getBytes());
                case "json" -> objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(objectMapper.readTree(file.getBytes()));
                case "pdf" -> parsePdf(bytes);
                case "docx" -> parseDocx(file.getBytes());
                case "xlsx", "xlsm", "xls" -> parseExcel(file.getBytes());
                case "png", "jpg", "jpeg", "bmp", "webp", "tif", "tiff" ->
                        ocrService.recognizeImage(bytes, file.getContentType());
                default -> throw new IllegalArgumentException("暂不支持的文件类型: " + extension);
            };
            rawText = rawText == null ? "" : rawText.strip();
            if (isImage(extension)) {
                ocrEngine = "multimodal-model";
                warnings.add("图片已通过当前启用的多模态模型完成OCR识别。");
            } else if ("pdf".equals(extension) && rawText.length() < 30) {
                JavaOcrService.OcrPdfResult ocr = ocrService.recognizePdf(bytes);
                rawText = ocr.text() == null ? "" : ocr.text().strip();
                pageCount = ocr.totalPages();
                ocrEngine = "pdfbox+multimodal-model";
                warnings.add("扫描版PDF已逐页渲染并通过多模态模型完成OCR识别。");
                if (ocr.truncated()) warnings.add("PDF超过10页，本次OCR仅识别前10页。");
            } else if ("pdf".equals(extension)) {
                pageCount = pdfPageCount(bytes);
            }
            if (rawText.isBlank()) throw new IllegalStateException("未识别出有效文字，请确认文件清晰度和当前模型是否支持图片理解");
            return new ParsedFileResult(filename, file.getContentType(), extension, rawText, rawText.length(),
                    warnings, ocrEngine, pageCount);
        } catch (IOException ex) {
            throw new IllegalStateException("Java文件解析失败: " + ex.getMessage(), ex);
        }
    }

    private String parsePdf(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private int pdfPageCount(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            return document.getNumberOfPages();
        }
    }

    private String parseDocx(byte[] bytes) throws IOException {
        List<String> lines = new ArrayList<>();
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            document.getParagraphs().forEach(paragraph -> {
                if (!paragraph.getText().isBlank()) lines.add(paragraph.getText());
            });
            document.getTables().forEach(table -> table.getRows().forEach(row ->
                    lines.add(String.join(" | ", row.getTableCells().stream().map(cell -> cell.getText().strip()).toList()))));
        }
        return String.join("\n", lines);
    }

    private String parseExcel(byte[] bytes) throws IOException {
        List<String> lines = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            for (Sheet sheet : workbook) {
                lines.add("工作表：" + sheet.getSheetName());
                for (Row row : sheet) {
                    List<String> cells = new ArrayList<>();
                    for (Cell cell : row) cells.add(formatter.formatCellValue(cell).strip());
                    if (cells.stream().anyMatch(value -> !value.isBlank())) lines.add(String.join(" | ", cells));
                }
            }
        }
        return String.join("\n", lines);
    }

    private String decode(byte[] bytes) {
        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        if (!utf8.contains("\uFFFD")) return utf8;
        return new String(bytes, Charset.forName("GB18030"));
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase();
    }

    private boolean isImage(String extension) {
        return List.of("png", "jpg", "jpeg", "bmp", "webp", "tif", "tiff").contains(extension);
    }
}
