package org.grosol.cspapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


/*
    "blockedURL": "inline",
    "columnNumber": 39,
    "disposition": "enforce",
    "documentURL": "https://example.com/csp-report",
    "effectiveDirective": "script-src-elem",
    "lineNumber": 121,
    "originalPolicy": "default-src 'self'; report-to csp-endpoint-name",
    "referrer": "https://www.google.com/",
    "sample": "console.log(\"lo\")",
    "sourceFile": "https://example.com/csp-report",
    "statusCode": 200
 */
public class CspReportBodyDTO {
    @JsonProperty("blockedURL")
    private String blockedURL;

    @JsonProperty("columnNumber")
    private int columnNumber;

    @JsonProperty("disposition")
    private String disposition;

    @JsonProperty("documentURL")
    private String documentURL;

    @JsonProperty("effectiveDirective")
    private String effectiveDirective;

    @JsonProperty("lineNumber")
    private int lineNumber;

    @JsonProperty("originalPolicy")
    private String originalPolicy;

    @JsonProperty("referrer")
    private String referrer;

    @JsonProperty("sample")
    private String sample;

    @JsonProperty("sourceFile")
    private String sourceFile;

    @JsonProperty("statusCode")
    private int statusCode;

    // Getters and Setters
    public String getBlockedURL() {
        return blockedURL;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public String getDisposition() {
        return disposition;
    }

    public String getDocumentURL() {
        return documentURL;
    }

    public String getEffectiveDirective() {
        return effectiveDirective;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getOriginalPolicy() {
        return originalPolicy;
    }

    public String getReferrer() {
        return referrer;
    }

    public String getSample() {
        return sample;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setBlockedURL(String blockedURL) {
        this.blockedURL = blockedURL;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public void setDocumentURL(String documentURL) {
        this.documentURL = documentURL;
    }

    public void setEffectiveDirective(String effectiveDirective) {
        this.effectiveDirective = effectiveDirective;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setOriginalPolicy(String originalPolicy) {
        this.originalPolicy = originalPolicy;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

}
