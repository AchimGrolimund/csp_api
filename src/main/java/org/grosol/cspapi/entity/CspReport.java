package org.grosol.cspapi.entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@MongoEntity(collection = "csp_reports")
public class CspReport extends PanacheMongoEntity {
    
    public ObjectId id;
    public int age;
    public String type;
    public String url;
    public String userAgent;
    public LocalDateTime createdAt;
    
    // Body fields (flattened for simplicity)
    public String blockedURL;
    public int columnNumber;
    public String disposition;
    public String documentURL;
    public String effectiveDirective;
    public int lineNumber;
    public String originalPolicy;
    public String referrer;
    public String sample;
    public String sourceFile;
    public int statusCode;
    public String ip;

    
    public CspReport() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Static methods for queries
    public static CspReport findByUrl(String url) {
        return find("url", url).firstResult();
    }
    
    public static long countByEffectiveDirective(String directive) {
        return count("effectiveDirective", directive);
    }
}
