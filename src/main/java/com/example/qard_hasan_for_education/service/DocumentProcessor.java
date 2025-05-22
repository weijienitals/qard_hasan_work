package com.example.qard_hasan_for_education.service;

import com.example.qard_hasan_for_education.model.SimpleBankInfo;
import com.example.qard_hasan_for_education.model.UniversityAcceptance;
import com.example.qard_hasan_for_education.model.ScholarshipAcceptance;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;

@Service
public class DocumentProcessor {

    @Value("${ai.gemini.api-key}")
    private String apiKey;

    @Value("${ai.gemini.base-url}")
    private String baseUrl;

    @Value("${ai.gemini.timeout:30000}")
    private int timeout;

    // Method for bank documents
    public SimpleBankInfo processBankDocument(MultipartFile pdfFile) throws Exception {
        String prompt = """
            Please analyze this bank document and extract ONLY these 4 pieces of information in JSON format:
            
            {
                "accountNumber": "string",
                "bankName": "string", 
                "accountHolderName": "string",
                "currentBalance": "number"
            }
            
            Instructions:
            - currentBalance should be the most recent balance shown
            - accountNumber should not include spaces or dashes
            - Return ONLY the JSON, no additional text
            """;

        return processDocument(pdfFile, prompt, SimpleBankInfo.class);
    }

    // Method for university acceptance letters
    public UniversityAcceptance processUniversityLetter(MultipartFile pdfFile) throws Exception {
        String prompt = """
            Please analyze this university acceptance letter and extract this information in JSON format:
            
            {
                "universityName": "string",
                "studentName": "string",
                "program": "string (degree program/major)",
                "acceptanceDate": "YYYY-MM-DD",
                "semesterStart": "string (when classes begin)"
            }
            
            Return ONLY the JSON, no additional text.
            """;

        return processDocument(pdfFile, prompt, UniversityAcceptance.class);
    }

    // Method for scholarship letters
    public ScholarshipAcceptance processScholarshipLetter(MultipartFile pdfFile) throws Exception {
        String prompt = """
            Please analyze this scholarship acceptance letter and extract this information in JSON format:
            
            {
                "scholarshipName": "string",
                "recipientName": "string",
                "amount": "number (scholarship amount)",
                "provider": "string (organization providing scholarship)",
                "academicYear": "string"
            }
            
            Return ONLY the JSON, no additional text.
            """;

        return processDocument(pdfFile, prompt, ScholarshipAcceptance.class);
    }

    // Generic method that handles the API call
    private <T> T processDocument(MultipartFile pdfFile, String prompt, Class<T> responseType) throws Exception {
        // Convert PDF to Base64
        String base64Pdf = Base64.getEncoder().encodeToString(pdfFile.getBytes());

        // Build request
        String requestBody = buildGeminiRequest(base64Pdf, prompt);

        // Send to Gemini
        String response = sendToGemini(requestBody);

        // Parse response
        return parseGeminiResponse(response, responseType);
    }

    private String buildGeminiRequest(String base64Pdf, String prompt) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // This creates the JSON structure that Gemini expects
        Map<String, Object> request = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();

        // Add the instruction text
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        parts.add(textPart);

        // Add the PDF file
        Map<String, Object> filePart = new HashMap<>();
        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("mimeType", "application/pdf");
        inlineData.put("data", base64Pdf);
        filePart.put("inlineData", inlineData);
        parts.add(filePart);

        content.put("parts", parts);
        contents.add(content);
        request.put("contents", contents);

        return mapper.writeValueAsString(request);
    }

    private String sendToGemini(String requestBody) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost post = new HttpPost(baseUrl + "?key=" + apiKey);
            post.setHeader("Content-Type", "application/json");

            post.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(post)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }

    private <T> T parseGeminiResponse(String response, Class<T> responseType) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        // Extract the AI's generated text from the response
        String generatedText = root.path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

        // Convert the JSON string back to your Java object
        return mapper.readValue(generatedText, responseType);
    }
}
