package org.grosol.cspapi.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.grosol.cspapi.dto.CspViolationReportDTO;
import org.grosol.cspapi.entity.CspReport;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CspReportService {

    private static final Logger LOGGER = Logger.getLogger(CspReportService.class);

    public CspReport saveReport(CspViolationReportDTO reportDTO, String clientIp) {
        LOGGER.info("Saving CSP report to MongoDB from IP: " + clientIp);
        
        try {
            // Create entity from DTO
            CspReport report = new CspReport();
            report.age = reportDTO.getAge();
            report.type = reportDTO.getType();
            report.url = reportDTO.getUrl();
            report.userAgent = reportDTO.getUserAgent();
            report.ip = clientIp; // Set the client IP
            
            // Map body fields
            if (reportDTO.getBody() != null) {
                report.blockedURL = reportDTO.getBody().getBlockedURL();
                report.columnNumber = reportDTO.getBody().getColumnNumber();
                report.disposition = reportDTO.getBody().getDisposition();
                report.documentURL = reportDTO.getBody().getDocumentURL();
                report.effectiveDirective = reportDTO.getBody().getEffectiveDirective();
                report.lineNumber = reportDTO.getBody().getLineNumber();
                report.originalPolicy = reportDTO.getBody().getOriginalPolicy();
                report.referrer = reportDTO.getBody().getReferrer();
                report.sample = reportDTO.getBody().getSample();
                report.sourceFile = reportDTO.getBody().getSourceFile();
                report.statusCode = reportDTO.getBody().getStatusCode();
            }
            
            // Persist to MongoDB
            report.persist();
            
            LOGGER.info("CSP report saved successfully with ID: " + report.id);
            return report;
            
        } catch (Exception e) {
            LOGGER.error("Error saving CSP report: " + e.getMessage(), e);
            throw new RuntimeException("Failed to save CSP report", e);
        }
    }
    
    public long getReportCount() {
        return CspReport.count();
    }
    
    public long getReportCountByDirective(String directive) {
        return CspReport.countByEffectiveDirective(directive);
    }
}
