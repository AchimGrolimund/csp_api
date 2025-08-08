package org.grosol.cspapi.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.grosol.cspapi.dto.CspViolationReportDTO;
import org.grosol.cspapi.dto.CspReportBodyDTO;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class CspResourceTest {

    @Test
    public void testGetTestEndpoint() {
        given()
          .when().get("/csp/test")
          .then()
             .statusCode(200)
             .body("message", is("Hello, World!"));
    }

    @Test
    public void testPostReportWithValidData() {
        CspViolationReportDTO report = createValidCspReport();
        
        given()
          .contentType(ContentType.JSON)
          .body(report)
          .when().post("/csp/report")
          .then()
             .statusCode(204);
    }

    @Test
    public void testPostReportWithNullBody() {
        given()
          .contentType(ContentType.JSON)
          .body("{}")
          .when().post("/csp/report")
          .then()
             .statusCode(204); // Service handles empty body gracefully
    }

    @Test
    public void testPostReportWithInvalidJson() {
        given()
          .contentType(ContentType.JSON)
          .body("{invalid json}")
          .when().post("/csp/report")
          .then()
             .statusCode(400);
    }

    @Test
    public void testPostReportWithXForwardedForHeader() {
        CspViolationReportDTO report = createValidCspReport();
        
        given()
          .contentType(ContentType.JSON)
          .header("X-Forwarded-For", "192.168.1.100, 10.0.0.1")
          .body(report)
          .when().post("/csp/report")
          .then()
             .statusCode(204);
    }

    @Test
    public void testPostReportWithXRealIPHeader() {
        CspViolationReportDTO report = createValidCspReport();
        
        given()
          .contentType(ContentType.JSON)
          .header("X-Real-IP", "203.0.113.42")
          .body(report)
          .when().post("/csp/report")
          .then()
             .statusCode(204);
    }

    @Test
    public void testPostReportWithCloudflareHeader() {
        CspViolationReportDTO report = createValidCspReport();
        
        given()
          .contentType(ContentType.JSON)
          .header("CF-Connecting-IP", "198.51.100.25")
          .body(report)
          .when().post("/csp/report")
          .then()
             .statusCode(204);
    }

    @Test
    public void testGetStatsEndpoint() {
        given()
          .when().get("/csp/stats")
          .then()
             .statusCode(200)
             .body("totalReports", notNullValue());
    }

    @Test
    public void testPostReportWithCompleteData() {
        CspViolationReportDTO report = createCompleteCspReport();
        
        given()
          .contentType(ContentType.JSON)
          .body(report)
          .when().post("/csp/report")
          .then()
             .statusCode(204);
    }

    private CspViolationReportDTO createValidCspReport() {
        CspViolationReportDTO report = new CspViolationReportDTO();
        report.setAge(53531);
        report.setType("csp-violation");
        report.setUrl("https://example.com/csp-report");
        report.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        
        CspReportBodyDTO body = new CspReportBodyDTO();
        body.setBlockedURL("inline");
        body.setColumnNumber(39);
        body.setDisposition("enforce");
        body.setDocumentURL("https://example.com/csp-report");
        body.setEffectiveDirective("script-src-elem");
        body.setLineNumber(121);
        body.setOriginalPolicy("default-src 'self'; report-to csp-endpoint-name");
        body.setReferrer("https://www.google.com/");
        body.setSample("console.log(\"test\")");
        body.setSourceFile("https://example.com/csp-report");
        body.setStatusCode(200);
        
        report.setBody(body);
        return report;
    }

    private CspViolationReportDTO createCompleteCspReport() {
        return createValidCspReport();
    }
}