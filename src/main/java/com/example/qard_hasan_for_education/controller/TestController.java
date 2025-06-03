package com.example.qard_hasan_for_education.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Test Redis connectivity
     */
    @GetMapping("/redis")
    public ResponseEntity<?> testRedis() {
        try {
            // Test basic Redis operations
            redisTemplate.opsForValue().set("test:key", "Redis is working!");
            String value = (String) redisTemplate.opsForValue().get("test:key");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Redis connection successful");
            response.put("testValue", value);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Redis connection failed: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Check what loan data exists in Redis
     */
    @GetMapping("/loans")
    public ResponseEntity<?> checkLoans() {
        try {
            Set<String> loanKeys = redisTemplate.keys("loan:*");
            Set<String> studentLoanKeys = redisTemplate.keys("student_loans:*");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("loanKeys", loanKeys);
            response.put("studentLoanKeys", studentLoanKeys);
            response.put("totalLoans", loanKeys != null ? loanKeys.size() : 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error checking loans: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get specific loan data
     */
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<?> checkSpecificLoan(@org.springframework.web.bind.annotation.PathVariable String loanId) {
        try {
            Object loanData = redisTemplate.opsForValue().get("loan:" + loanId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("loanExists", loanData != null);
            response.put("loanData", loanData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error checking specific loan: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }


    @GetMapping("/transactions")
    public ResponseEntity<?> checkTransactions() {
        try {
            Set<String> transactionKeys = redisTemplate.keys("transaction:*");
            Set<String> loanTransactionKeys = redisTemplate.keys("loan_transactions:*");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactionKeys", transactionKeys);
            response.put("loanTransactionKeys", loanTransactionKeys);
            response.put("totalTransactions", transactionKeys != null ? transactionKeys.size() : 0);

            // Get actual transaction data
            if (transactionKeys != null && !transactionKeys.isEmpty()) {
                Map<String, Object> transactionData = new HashMap<>();
                for (String key : transactionKeys) {
                    Object txn = redisTemplate.opsForValue().get(key);
                    transactionData.put(key, txn);
                }
                response.put("transactionData", transactionData);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}