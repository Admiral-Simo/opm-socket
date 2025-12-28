package com.network.opmsocket.backend.chat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.test.context.SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
public class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final Path uploads = Paths.get("uploads");

    @AfterEach
    public void cleanup() throws IOException {
        if (Files.exists(uploads)) {
            Files.list(uploads).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    // ignore
                }
            });
            try {
                Files.deleteIfExists(uploads);
            } catch (IOException ignored) {
            }
        }
    }

    @Test
    public void uploadPng_shouldSucceed() throws Exception {
        byte[] pngHeader = new byte[] {(byte)137, 80, 78, 71, 13, 10, 26, 10, 0};
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", pngHeader);

        mockMvc.perform(multipart("/upload").file(file).with(jwt().jwt(jwt -> jwt.claim("preferred_username", "testuser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.url", startsWith("/uploads/")));
    }

    @Test
    public void uploadTooLarge_shouldReject() throws Exception {
        byte[] big = new byte[1_048_577]; // > 1MB
        MockMultipartFile file = new MockMultipartFile("file", "big.jpg", "image/jpeg", big);

        mockMvc.perform(multipart("/upload").file(file).with(jwt().jwt(jwt -> jwt.claim("preferred_username", "testuser"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File too large. Max size is 1MB."));
    }

    @Test
    public void uploadBadType_shouldReject() throws Exception {
        byte[] data = new byte[] {0,1,2,3,4};
        MockMultipartFile file = new MockMultipartFile("file", "bad.bin", "application/octet-stream", data);

        mockMvc.perform(multipart("/upload").file(file).with(jwt().jwt(jwt -> jwt.claim("preferred_username", "testuser"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File type not allowed"));
    }

    @Test
    public void uploadDocx_shouldSucceed() throws Exception {
        // ZIP header => OOXML (.docx/.xlsx/.pptx)
        byte[] zipHeader = new byte[] { 'P', 'K', 3, 4, 0, 0, 0, 0 };
        MockMultipartFile file = new MockMultipartFile("file", "doc.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", zipHeader);

        mockMvc.perform(multipart("/upload").file(file).with(jwt().jwt(jwt -> jwt.claim("preferred_username", "testuser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists());
    }

    @Test
    public void uploadOleDoc_shouldSucceed() throws Exception {
        // OLE Compound file header (older .doc/.xls)
        byte[] oleHeader = new byte[] {(byte)0xD0, (byte)0xCF, 0x11, (byte)0xE0, (byte)0xA1, (byte)0xB1, 0x1A, (byte)0xE1 };
        MockMultipartFile file = new MockMultipartFile("file", "old.doc", "application/msword", oleHeader);

        mockMvc.perform(multipart("/upload").file(file).with(jwt().jwt(jwt -> jwt.claim("preferred_username", "testuser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists());
    }

    @Test
    public void uploadBinaryText_shouldReject() throws Exception {
        // content-type text/plain but binary content -> should be rejected by heuristic
        byte[] bin = new byte[256];
        for (int i = 0; i < bin.length; i++) bin[i] = 0;
        MockMultipartFile file = new MockMultipartFile("file", "weird.txt", "text/plain", bin);

        mockMvc.perform(multipart("/upload").file(file).with(jwt().jwt(jwt -> jwt.claim("preferred_username", "testuser"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File type not allowed"));
    }
}
