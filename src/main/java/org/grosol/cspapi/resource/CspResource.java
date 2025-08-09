package org.grosol.cspapi.resource;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grosol.cspapi.dto.CspViolationReportDTO;
import org.grosol.cspapi.service.CspReportService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

import io.smallrye.common.annotation.RunOnVirtualThread;


@Path("/csp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "CSP Resource", description = "Handles Content Security Policy (CSP) reports and related operations.")
@RunOnVirtualThread
public class CspResource {

    private static final Logger LOGGER = Logger.getLogger(CspResource.class);

    @Inject
    CspReportService cspReportService;

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "unknown")
    String applicationVersion;

    /**
     * Extracts the real client IP address considering proxy headers
     */
    private String getClientIpAddress(ContainerRequestContext requestContext) {
        String xForwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = requestContext.getHeaderString("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        String xForwardedForCloudflare = requestContext.getHeaderString("CF-Connecting-IP");
        if (xForwardedForCloudflare != null && !xForwardedForCloudflare.isEmpty()) {
            return xForwardedForCloudflare;
        }

        // Fallback - try to get from request properties or use "unknown"
        return "unknown";
    }


    @GET
    @Path("/test")
    public Response getTest() {
        LOGGER.info("Calling getTest method");
        return Response.ok("{\"message\":\"Hello, World!\"}").build();
    }

    @GET
    @Path("/version")
    public Response getVersion() {
        LOGGER.info("Calling getVersion method");
        return Response.ok("{\"version\":\"" + applicationVersion + "\"}").build();
    }

    @POST
    @Path("/report")
    public Response postReport(CspViolationReportDTO reportDTO, @Context ContainerRequestContext requestContext) {
        LOGGER.info("Received CSP violation report");
        
        // Basic validation
        if (reportDTO == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity("{\"error\":\"Empty report data\"}")
                          .build();
        }
        
        try {
            // Extract client IP address
            String clientIp = getClientIpAddress(requestContext);
            LOGGER.info("Client IP: " + clientIp);
            
            // Call the service to save the report to MongoDB
            cspReportService.saveReport(reportDTO, clientIp);
            
            // Return 204 No Content as per CSP spec
            return Response.noContent().build();
            
        } catch (Exception e) {
            LOGGER.error("Failed to save CSP report: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\":\"Failed to save CSP report\"}")
                          .build();
        }
    }

    @GET
    @Path("/stats")
    public Response getStats() {
        LOGGER.info("Getting CSP report statistics");
        
        try {
            long totalReports = cspReportService.getReportCount();
            
            return Response.ok("{\"totalReports\":" + totalReports + "}")
                          .build();
                          
        } catch (Exception e) {
            LOGGER.error("Failed to get CSP statistics: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\":\"Failed to get statistics\"}")
                          .build();
        }
    }

}