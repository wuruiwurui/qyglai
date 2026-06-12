package com.qyglai.automation.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.qyglai.automation.entity.AiModelCallLogEntity;
import com.qyglai.automation.mapper.AiModelCallLogMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * Java OCR 服务。
 *
 * <p>图片直接发送给当前启用的多模态模型；扫描 PDF 先由 PDFBox 渲染为图片，
 * 再逐页识别。整个过程不依赖 Python 服务。</p>
 */
@Service
public class JavaOcrService {

    private static final int MAX_PDF_PAGES = 10;
    private static final float PDF_RENDER_DPI = 180F;
    private final JavaAiModelGateway modelGateway;
    private final AiModelCallLogMapper callLogMapper;

    public JavaOcrService(JavaAiModelGateway modelGateway, AiModelCallLogMapper callLogMapper) {
        this.modelGateway = modelGateway;
        this.callLogMapper = callLogMapper;
    }

    /**
     * 识别单张图片中的文字。
     *
     * @param bytes 图片字节
     * @param mimeType 图片 MIME 类型
     * @return OCR 文字
     */
    public String recognizeImage(byte[] bytes, String mimeType) {
        long start = System.currentTimeMillis();
        try {
            String result = modelGateway.generateVisionText(ocrPrompt(), bytes, normalizeMimeType(mimeType));
            recordCall(bytes, start, true, null);
            return result;
        } catch (RuntimeException exception) {
            recordCall(bytes, start, false, exception.getMessage());
            throw exception;
        }
    }

    /**
     * 识别扫描 PDF 中的文字。
     *
     * @param bytes PDF 字节
     * @return 按页组合后的 OCR 文字
     */
    public OcrPdfResult recognizePdf(byte[] bytes) throws IOException {
        List<String> pages = new ArrayList<>();
        int totalPages;
        try (PDDocument document = Loader.loadPDF(bytes)) {
            totalPages = document.getNumberOfPages();
            PDFRenderer renderer = new PDFRenderer(document);
            int pagesToRead = Math.min(totalPages, MAX_PDF_PAGES);
            for (int pageIndex = 0; pageIndex < pagesToRead; pageIndex++) {
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, PDF_RENDER_DPI, ImageType.RGB);
                try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    ImageIO.write(image, "jpg", output);
                    String text = recognizeImage(output.toByteArray(), "image/jpeg");
                    if (text != null && !text.isBlank()) {
                        pages.add("第 " + (pageIndex + 1) + " 页\n" + text.strip());
                    }
                }
            }
        }
        return new OcrPdfResult(String.join("\n\n", pages), totalPages, totalPages > MAX_PDF_PAGES);
    }

    private String ocrPrompt() {
        return """
                你是企业文档 OCR 引擎。请完整识别图片中的所有可见文字，并尽量保持原有阅读顺序和换行。
                金额、日期、发票号码、税号、合同编号等关键字段必须逐字符核对，不得猜测或改写。
                只输出识别到的原始文字，不要解释，不要添加 Markdown 标记；无法确认的字符使用 [?]。
                """;
    }

    private String normalizeMimeType(String mimeType) {
        return mimeType == null || !mimeType.startsWith("image/") ? "image/png" : mimeType;
    }

    private void recordCall(byte[] bytes, long start, boolean success, String errorMessage) {
        try {
            AiModelCallLogEntity log = new AiModelCallLogEntity();
            log.setId(IdWorker.getId());
            log.setScenario("document_ocr");
            log.setBusinessType("file");
            log.setModelName(modelGateway.status().model());
            log.setPromptTemplateCode("java-direct/document-ocr");
            log.setRequestTokens(0);
            log.setResponseTokens(0);
            log.setCostAmount(BigDecimal.ZERO);
            log.setLatencyMs((int) Math.max(0, System.currentTimeMillis() - start));
            log.setSuccessFlag(success ? 1 : 0);
            log.setErrorMessage(errorMessage == null ? null : errorMessage.substring(0, Math.min(1000, errorMessage.length())));
            log.setRequestHash(DigestUtils.md5DigestAsHex(
                    ("ocr:" + bytes.length).getBytes(StandardCharsets.UTF_8)));
            log.setCreatedAt(LocalDateTime.now());
            callLogMapper.insert(log);
        } catch (RuntimeException ignored) {
            // OCR 日志失败不能阻塞文件识别主流程。
        }
    }

    /**
     * 扫描 PDF OCR 结果。
     *
     * @param text 识别文字
     * @param totalPages PDF 总页数
     * @param truncated 是否因页数限制而截断
     */
    public record OcrPdfResult(String text, int totalPages, boolean truncated) {
    }
}
