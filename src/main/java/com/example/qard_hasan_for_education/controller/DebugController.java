package com.example.qard_hasan_for_education.controller;

import com.example.qard_hasan_for_education.model.LoanAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class DebugController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/test-redis-storage")
    public ResponseEntity<?> testRedisStorage() {
        try {
            // Create a simple test loan
            LoanAccount testLoan = new LoanAccount(
                    "TEST_STUDENT",
                    "TEST_APP",
                    new BigDecimal("10000"),
                    12,
                    "Test Student",
                    "Test University",
                    "Test Program",
                    "Test Country",
                    "Indonesian"
            );

            String key = "test_loan:" + testLoan.getLoanId();

            Map<String, Object> result = new HashMap<>();
            result.put("testLoanId", testLoan.getLoanId());
            result.put("key", key);

            // Step 1: Store the loan
            redisTemplate.opsForValue().set(key, testLoan, Duration.ofMinutes(5));
            result.put("storageResult", "SUCCESS");

            // Step 2: Immediate retrieval
            Object immediate = redisTemplate.opsForValue().get(key);
            result.put("immediateRetrieval", immediate != null ? "SUCCESS" : "FAILED");
            result.put("immediateType", immediate != null ? immediate.getClass().getSimpleName() : "null");

            // Step 3: Check if key exists
            Boolean exists = redisTemplate.hasKey(key);
            result.put("keyExists", exists);

            // Step 4: Wait 2 seconds and try again
            Thread.sleep(2000);
            Object delayed = redisTemplate.opsForValue().get(key);
            result.put("delayedRetrieval", delayed != null ? "SUCCESS" : "FAILED");
            result.put("delayedType", delayed != null ? delayed.getClass().getSimpleName() : "null");

            // Step 5: Type check
            result.put("immediateIsLoanAccount", immediate instanceof LoanAccount);
            result.put("delayedIsLoanAccount", delayed instanceof LoanAccount);

            // Step 6: Show all keys
            Set<String> allKeys = redisTemplate.keys("*");
            result.put("allRedisKeys", allKeys);

            return ResponseEntity.ok(Map.of("success", true, "results", result));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/redis-info")
    public ResponseEntity<?> getRedisInfo() {
        try {
            Map<String, Object> info = new HashMap<>();

            // Get all keys
            Set<String> allKeys = redisTemplate.keys("*");
            info.put("totalKeys", allKeys.size());
            info.put("allKeys", allKeys);

            // Get loan keys specifically
            Set<String> loanKeys = redisTemplate.keys("loan:*");
            info.put("loanKeys", loanKeys);

            // Test basic Redis operations
            redisTemplate.opsForValue().set("debug:test", "test_value", Duration.ofMinutes(1));
            String testRetrieve = (String) redisTemplate.opsForValue().get("debug:test");
            info.put("basicRedisTest", testRetrieve != null ? "SUCCESS" : "FAILED");

            return ResponseEntity.ok(Map.of("success", true, "info", info));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/test-loan-retrieval/{loanId}")
    public ResponseEntity<?> testSpecificLoanRetrieval(@PathVariable String loanId) {
        try {
            String key = "loan:" + loanId;
            Map<String, Object> result = new HashMap<>();

            // Check existence
            Boolean exists = redisTemplate.hasKey(key);
            result.put("keyExists", exists);
            result.put("key", key);

            if (exists) {
                // Get raw object
                Object raw = redisTemplate.opsForValue().get(key);
                result.put("rawObjectNull", raw == null);
                result.put("rawObjectType", raw != null ? raw.getClass().getName() : "null");

                if (raw != null) {
                    result.put("isLoanAccount", raw instanceof LoanAccount);
                    result.put("rawContent", raw.toString());
                }
            }

            return ResponseEntity.ok(Map.of("success", true, "result", result));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}