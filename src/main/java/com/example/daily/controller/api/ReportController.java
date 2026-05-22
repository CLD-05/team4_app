package com.example.daily.controller.api;

import com.example.daily.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyReport(@AuthenticationPrincipal UserDetails userDetails,
                                                                 @RequestParam int year,
                                                                 @RequestParam int month) {
        return ResponseEntity.ok(reportService.getMonthlyReport(userDetails.getUsername(), year, month));
    }
}
