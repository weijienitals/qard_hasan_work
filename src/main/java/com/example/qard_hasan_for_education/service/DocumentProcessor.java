// Updated DocumentProcessor.java
package com.example.qard_hasan_for_education.service;

import com.example.qard_hasan_for_education.model.individual.PassportInfo;
import com.example.qard_hasan_for_education.model.individual.SimpleBankInfo;
import com.example.qard_hasan_for_education.model.individual.UniversityAcceptance;
import com.example.qard_hasan_for_education.model.individual.ScholarshipAcceptance;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;

@Service
public class DocumentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);

    @Value("${ai.gemini.api-key}")
    private String apiKey;

    @Value("${ai.gemini.base-url}")
    private String baseUrl;

    @Value("${ai.gemini.timeout:30000}")
    private int timeout;

    // Enhanced method for bank documents with risk assessment
    public SimpleBankInfo processBankDocument(MultipartFile pdfFile) throws Exception {
        String prompt = """
            Please analyze this bank document and extract information including risk assessment in JSON format:
            
            {
                "accountNumber": "string",
                "bankName": "string",
                "accountHolderName": "string",
                "currentBalance": "number",
                "purchasingPower": "string",
                "incomeStability": "stable/irregular/declining",
                "expenseRatio": "number (percentage of income spent)",
                "savingsTrend": "increasing/stable/decreasing",
                "overdraftCount": "number (count of overdrafts in last 6 months)",
                "repaymentCapacity": "excellent/good/fair/poor",
                "monthlyIncome": "number (average monthly income)",
                "monthlyExpenses": "number (average monthly expenses)",
                "riskFactors": ["list of specific financial risk concerns"],
                "transactions": ["list of recent transactions"]
            }
            
            Instructions:
            - Analyze transaction patterns to determine income stability
            - Calculate expense ratio based on income vs expenses
            - Identify overdrafts, bounced payments, or negative balances
            - Assess savings growth or decline trend
            - Identify risk factors like irregular income, high expenses, frequent overdrafts
            - For repayment capacity, consider: excellent (>30% disposable income), good (20-30%), fair (10-20%), poor (<10%)
            - Return ONLY the JSON, no additional text
            """;

        return processDocument(pdfFile, prompt, SimpleBankInfo.class);
    }

    // Enhanced method for university acceptance letters with risk assessment
    public UniversityAcceptance processUniversityLetter(MultipartFile pdfFile) throws Exception {
        String prompt = """
            Please analyze this university acceptance letter and extract information including risk assessment in JSON format:
            
            {
                "universityName": "string",
                "studentName": "string",
                "program": "string (degree program/major)",
                "acceptanceDate": "YYYY-MM-DD",
                "semesterStart": "string (when classes begin)",
                "universityTier": "top-tier/mid-tier/lower-tier",
                "programMarketability": "high/medium/low",
                "completionProbability": "very-high/high/medium/low",
                "universityRanking": "string (if mentioned or if you know it)",
                "riskFactors": ["list of academic risk concerns"]
            }
            
            Instructions:
            - Assess university tier based on reputation and ranking (Stanford, MIT = top-tier; state schools = mid-tier; unknown/unaccredited = lower-tier)
            - Evaluate program marketability (Engineering, CS, Medicine = high; Business, Liberal Arts = medium; Niche fields = low)
            - Estimate completion probability based on program difficulty and university selectivity
            - Identify risk factors like conditional acceptance, probationary status, or challenging programs
            - Return ONLY the JSON, no additional text
            """;

        return processDocument(pdfFile, prompt, UniversityAcceptance.class);
    }

    // Enhanced method for scholarship letters with risk assessment
    public ScholarshipAcceptance processScholarshipLetter(MultipartFile pdfFile) throws Exception {
        String prompt = """
            Please analyze this scholarship acceptance letter and extract information including risk assessment in JSON format:
            
            {
                "scholarshipName": "string",
                "recipientName": "string",
                "amount": "number (scholarship amount)",
                "provider": "string (organization providing scholarship)",
                "academicYear": "string",
                "isValidScholarship": boolean,
                "fundingGapRisk": "none/low/medium/high",
                "providerCredibility": "verified/questionable/unknown",
                "documentAuthenticity": "verified/suspicious/likely-fake",
                "riskFactors": ["list of scholarship-related risk concerns"]
            }
            
            Instructions:
            - Verify if the scholarship name and provider are legitimate and well-known
            - Check if the amount seems reasonable for the type of scholarship
            - Assess funding gap risk based on scholarship amount vs typical education costs
            - Evaluate provider credibility (government agencies, major foundations = verified; unknown organizations = questionable)
            - Look for signs of document fraud (poor formatting, spelling errors, unrealistic amounts)
            - Identify risk factors like partial funding, conditional terms, or suspicious providers
            - Return ONLY the JSON, no additional text
            """;

        return processDocument(pdfFile, prompt, ScholarshipAcceptance.class);
    }

    // Passport processing remains the same as it doesn't need risk assessment
    public PassportInfo processPassportImage(MultipartFile imageFile) throws Exception {
        String prompt = """
        Please analyze this image and extract personal information in JSON format:
        
        {
            "fullName": "string (full name as shown on passport)",
            "identification": "string (passport or identification number)",
            "nationality": "string (nationality/citizenship)",
            "dateOfBirth": "YYYY-MM-DD (date of birth)",
            "gender": "string (M/F/Male/Female)",
            "expiryDate": "YYYY-MM-DD (passport expiry date)"
        }
        
        Instructions:
        - identify if image is a passport or an identification card
        - if it is a passport, identification field value should be passport number
        - if it is a identification card, identification field value should be identification number
        - there should not be key value called passport number in json data returned
        
        - Return ONLY the JSON, no additional text
        """;

        return processImageDocument(imageFile, prompt, PassportInfo.class);
    }

    // Add this new method for image processing
    private <T> T processImageDocument(MultipartFile imageFile, String prompt, Class<T> responseType) throws Exception {
        String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
        logger.info("Image file size: {} bytes, type: {}", imageFile.getBytes().length, imageFile.getContentType());

        String requestBody = buildGeminiRequestForImage(base64Image, imageFile.getContentType(), prompt);
        String response = sendToGemini(requestBody);
        return parseGeminiResponse(response, responseType);
    }

    // Add this new method for building image requests
    private String buildGeminiRequestForImage(String base64Image, String mimeType, String prompt) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> request = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();

        // Add the instruction text
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        parts.add(textPart);

        // Add the image file
        Map<String, Object> filePart = new HashMap<>();
        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("mimeType", mimeType);
        inlineData.put("data", base64Image);
        filePart.put("inlineData", inlineData);
        parts.add(filePart);

        content.put("parts", parts);
        contents.add(content);
        request.put("contents", contents);

        return mapper.writeValueAsString(request);
    }

    // Generic method that handles the API call
    private <T> T processDocument(MultipartFile pdfFile, String prompt, Class<T> responseType) throws Exception {
        // Convert PDF to Base64
        String base64Pdf = Base64.getEncoder().encodeToString(pdfFile.getBytes());
        logger.info("PDF file size: {} bytes", pdfFile.getBytes().length);

        // Build request
        String requestBody = buildGeminiRequest(base64Pdf, prompt);
        logger.debug("Request body: {}", requestBody);

        // Send to Gemini
        String response = sendToGemini(requestBody);
        logger.info("Gemini API response: {}", response);

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
        int maxRetries = 3;
        int retryDelayMs = 1000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                String fullUrl = baseUrl + "?key=" + apiKey;
                logger.info("Calling Gemini API at: {} (attempt {}/{})", baseUrl + "?key=***", attempt, maxRetries);

                HttpPost post = new HttpPost(fullUrl);
                post.setHeader("Content-Type", "application/json");

                // Set timeout configurations
                post.setConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                        .setConnectionRequestTimeout(org.apache.hc.core5.util.Timeout.ofMilliseconds(timeout))
                        .setResponseTimeout(org.apache.hc.core5.util.Timeout.ofMilliseconds(timeout))
                        .build());

                post.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

                try (CloseableHttpResponse response = client.execute(post)) {
                    int statusCode = response.getCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    logger.info("Gemini API status code: {}", statusCode);

                    if (statusCode == 200) {
                        logger.info("Gemini API success on attempt {}", attempt);
                        return responseBody;
                    } else if (statusCode == 429) {
                        logger.warn("Rate limit hit on attempt {}/{}", attempt, maxRetries);
                        if (attempt < maxRetries) {
                            logger.info("Waiting {} ms before retry...", retryDelayMs);
                            Thread.sleep(retryDelayMs);
                            retryDelayMs *= 2;
                            continue;
                        }
                    }

                    logger.error("Gemini API response body: {}", responseBody);
                    throw new Exception("Gemini API returned status " + statusCode + ": " + responseBody);
                }
            } catch (java.net.SocketTimeoutException e) {
                logger.error("Socket timeout on attempt {}/{}: {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    logger.info("Retrying after socket timeout...");
                    Thread.sleep(retryDelayMs);
                    retryDelayMs *= 2;
                    continue;
                }
                throw new Exception("Socket timeout after " + maxRetries + " attempts: " + e.getMessage(), e);
            } catch (java.net.ConnectException e) {
                logger.error("Connection failed on attempt {}/{}: {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    logger.info("Retrying after connection failure...");
                    Thread.sleep(retryDelayMs);
                    retryDelayMs *= 2;
                    continue;
                }
                throw new Exception("Connection failed after " + maxRetries + " attempts: " + e.getMessage(), e);
            } catch (java.net.UnknownHostException e) {
                logger.error("DNS resolution failed: {}", e.getMessage());
                throw new Exception("Cannot resolve hostname generativelanguage.googleapis.com: " + e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Unexpected error on attempt {}/{}: {} - {}", attempt, maxRetries, e.getClass().getSimpleName(), e.getMessage());
                if (attempt < maxRetries) {
                    logger.info("Retrying after unexpected error...");
                    Thread.sleep(retryDelayMs);
                    retryDelayMs *= 2;
                    continue;
                }
                throw new Exception("Failed after " + maxRetries + " attempts: " + e.getMessage(), e);
            }
        }

        throw new Exception("Failed after " + maxRetries + " attempts");
    }

    private <T> T parseGeminiResponse(String response, Class<T> responseType) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        logger.debug("Parsing Gemini response: {}", response);

        // Check if response has error
        if (root.has("error")) {
            String errorMessage = root.path("error").path("message").asText();
            throw new Exception("Gemini API error: " + errorMessage);
        }

        // Check if candidates exist
        if (!root.has("candidates") || root.path("candidates").isEmpty()) {
            throw new Exception("No candidates found in response: " + response);
        }

        JsonNode candidates = root.path("candidates");
        if (candidates.get(0) == null) {
            throw new Exception("First candidate is null in response: " + response);
        }

        JsonNode firstCandidate = candidates.get(0);
        if (!firstCandidate.has("content")) {
            throw new Exception("No content found in first candidate: " + response);
        }

        JsonNode content = firstCandidate.path("content");
        if (!content.has("parts") || content.path("parts").isEmpty()) {
            throw new Exception("No parts found in content: " + response);
        }

        JsonNode parts = content.path("parts");
        if (parts.get(0) == null) {
            throw new Exception("First part is null: " + response);
        }

        JsonNode firstPart = parts.get(0);
        if (!firstPart.has("text")) {
            throw new Exception("No text found in first part: " + response);
        }

        // Extract the AI's generated text from the response
        String generatedText = firstPart.path("text").asText();
        logger.info("Generated text from Gemini: {}", generatedText);

        // Clean the response - remove markdown code blocks if present
        String cleanedJson = cleanJsonResponse(generatedText);
        logger.info("Cleaned JSON: {}", cleanedJson);

        // Convert the JSON string back to your Java object
        try {
            return mapper.readValue(cleanedJson, responseType);
        } catch (Exception e) {
            logger.error("Failed to parse cleaned JSON: {}", cleanedJson);
            throw new Exception("Failed to parse AI response as JSON: " + cleanedJson, e);
        }
    }

    /**
     * Clean the JSON response by removing markdown code blocks and extra whitespace
     */
    private String cleanJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return response;
        }

        String cleaned = response.trim();

        // Remove markdown code blocks (```json ... ``` or ``` ... ```)
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7); // Remove "```json"
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3); // Remove "```"
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3); // Remove ending "```"
        }

        return cleaned.trim();
    }
}