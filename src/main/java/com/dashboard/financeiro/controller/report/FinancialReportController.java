package com.dashboard.financeiro.controller.report;

import com.dashboard.financeiro.dto.report.ReportDateRangeRequest;
import com.dashboard.financeiro.service.report.FinancialReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FinancialReportController {

    @Autowired
    private FinancialReportService financialReportService;

    @GetMapping("/financial")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> generateFinancialReport(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            // Configurar datas padrão se não fornecidas
            if (startDate == null) {
                startDate = LocalDate.now().withDayOfMonth(1);
            }
            
            if (endDate == null) {
                endDate = startDate.plusMonths(1).minusDays(1);
            }
            
            // Gerar o relatório
            ByteArrayOutputStream baos = financialReportService.generateFinancialReport(
                    authentication.getName(), startDate, endDate);
            
            // Criar nome do arquivo baseado no período
            String fileName = "relatorio_financeiro_" + 
                    startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                    endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            
            // Configurar o cabeçalho para download do PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/financial")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> generateFinancialReportWithBody(
            Authentication authentication,
            @RequestBody ReportDateRangeRequest request) {
        
        try {
            LocalDate startDate = request.getStartDate();
            LocalDate endDate = request.getEndDate();
            
            // Definir datas com base no tipo de relatório
            if ("monthly".equals(request.getReportType())) {
                startDate = LocalDate.now().withDayOfMonth(1);
                endDate = startDate.plusMonths(1).minusDays(1);
            } else if ("annual".equals(request.getReportType())) {
                startDate = LocalDate.now().withDayOfYear(1);
                endDate = startDate.plusYears(1).minusDays(1);
            } else {
                // Para tipo "custom", usar as datas fornecidas
                if (startDate == null) {
                    startDate = LocalDate.now().withDayOfMonth(1);
                }
                
                if (endDate == null) {
                    endDate = startDate.plusMonths(1).minusDays(1);
                }
            }
            
            // Gerar o relatório
            ByteArrayOutputStream baos = financialReportService.generateFinancialReport(
                    authentication.getName(), startDate, endDate);
            
            // Criar nome do arquivo baseado no tipo e período
            String fileName;
            if ("monthly".equals(request.getReportType())) {
                fileName = "relatorio_financeiro_mensal_" + 
                        startDate.format(DateTimeFormatter.ofPattern("yyyyMM")) + ".pdf";
            } else if ("annual".equals(request.getReportType())) {
                fileName = "relatorio_financeiro_anual_" + 
                        startDate.format(DateTimeFormatter.ofPattern("yyyy")) + ".pdf";
            } else {
                fileName = "relatorio_financeiro_" + 
                        startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                        endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            }
            
            // Configurar o cabeçalho para download do PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/financial/monthly")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> generateMonthlyFinancialReport(Authentication authentication) {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        try {
            // Gerar o relatório
            ByteArrayOutputStream baos = financialReportService.generateFinancialReport(
                    authentication.getName(), startDate, endDate);
            
            // Criar nome do arquivo baseado no mês
            String fileName = "relatorio_financeiro_mensal_" + 
                    startDate.format(DateTimeFormatter.ofPattern("yyyyMM")) + ".pdf";
            
            // Configurar o cabeçalho para download do PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/financial/annual")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> generateAnnualFinancialReport(Authentication authentication) {
        LocalDate startDate = LocalDate.now().withDayOfYear(1);
        LocalDate endDate = startDate.plusYears(1).minusDays(1);
        
        try {
            // Gerar o relatório
            ByteArrayOutputStream baos = financialReportService.generateFinancialReport(
                    authentication.getName(), startDate, endDate);
            
            // Criar nome do arquivo baseado no ano
            String fileName = "relatorio_financeiro_anual_" + 
                    startDate.format(DateTimeFormatter.ofPattern("yyyy")) + ".pdf";
            
            // Configurar o cabeçalho para download do PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
