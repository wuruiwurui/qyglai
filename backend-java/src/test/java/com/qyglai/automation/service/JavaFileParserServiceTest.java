package com.qyglai.automation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class JavaFileParserServiceTest {

    private JavaOcrService ocrService;
    private JavaFileParserService parserService;

    @BeforeEach
    void setUp() {
        ocrService = mock(JavaOcrService.class);
        parserService = new JavaFileParserService(new ObjectMapper(), ocrService);
    }

    @Test
    void shouldRouteImageToOcr() {
        when(ocrService.recognizeImage(any(byte[].class), anyString())).thenReturn("发票金额 1160 元");
        MockMultipartFile file = new MockMultipartFile("file", "invoice.png", "image/png", new byte[] {1, 2, 3});

        var result = parserService.parse(file);

        assertThat(result.rawText()).contains("1160");
        assertThat(result.ocrEngine()).isEqualTo("multimodal-model");
        assertThat(result.warnings()).isNotEmpty();
    }

    @Test
    void shouldRouteScannedPdfToOcr() throws Exception {
        when(ocrService.recognizePdf(any(byte[].class)))
                .thenReturn(new JavaOcrService.OcrPdfResult("扫描合同正文", 1, false));
        byte[] pdf;
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(output);
            pdf = output.toByteArray();
        }
        MockMultipartFile file = new MockMultipartFile("file", "scan.pdf", "application/pdf", pdf);

        var result = parserService.parse(file);

        assertThat(result.rawText()).isEqualTo("扫描合同正文");
        assertThat(result.ocrEngine()).isEqualTo("pdfbox+multimodal-model");
        assertThat(result.pageCount()).isEqualTo(1);
    }
}
