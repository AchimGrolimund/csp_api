# Release Build Guide

## Problem
Beim Erstellen von Release-Images wird die Version in der Anwendung immer noch als `1.x.x-SNAPSHOT` angezeigt, obwohl der Docker-Tag korrekt ohne `-SNAPSHOT` erstellt wird.

## Ursache
Die `pom.xml` behält während des Build-Prozesses die `-SNAPSHOT` Version bei, wodurch die Anwendung zur Laufzeit die SNAPSHOT-Version anzeigt.

## Lösungsansätze

### Lösung 1: Temporäre pom.xml Anpassung (build_dockerfile.sh)

**Funktionsweise:**
- Temporär wird die `pom.xml` vor dem Docker Build auf die Release-Version gesetzt
- Nach dem Build wird die ursprüngliche Version wiederhergestellt
- Nutzt `mvnw versions:set` und `mvnw versions:revert`

**Vorteile:**
- Einfach zu verstehen
- Funktioniert mit bestehender Maven-Konfiguration

**Nachteile:**
- Manipuliert die `pom.xml` während des Builds
- Bei Build-Fehlern könnte die `pom.xml` in einem inkonsistenten Zustand bleiben

**Verwendung:**
```bash
# Snapshot Build (mit -SNAPSHOT in der App)
./build_dockerfile.sh

# Release Build (ohne -SNAPSHOT in der App)
./build_dockerfile.sh --release
```

### Lösung 2: Maven-Profile (build_dockerfile_profile.sh) **[EMPFOHLEN]**

**Funktionsweise:**
- Nutzt Maven-Profile zur Build-Zeit-Konfiguration
- Das `release`-Profil entfernt automatisch `-SNAPSHOT` aus der `release.version` Property
- Resource-Filtering ersetzt `@release.version@` in `application.properties`

**Vorteile:**
- Saubere Maven-Integration
- Keine Manipulation der `pom.xml`
- Robuster und weniger fehleranfällig
- Bessere Trennung zwischen Development und Release

**Nachteile:**
- Etwas komplexere Maven-Konfiguration

**Verwendung:**
```bash
# Snapshot Build (mit -SNAPSHOT in der App)
./build_dockerfile_profile.sh

# Release Build (ohne -SNAPSHOT in der App)
./build_dockerfile_profile.sh --release
```

## Implementierte Funktionen

### Version-Endpoint
Die Anwendung bietet einen neuen Endpoint zur Abfrage der aktuellen Version:

```bash
curl http://localhost:8080/csp/version
```

**Response:**
```json
{
  "version": "1.0.1"          // Bei Release-Build
}
```

oder

```json
{
  "version": "1.0.1-SNAPSHOT" // Bei Snapshot-Build
}
```

### Automatische Versionsverwaltung
Beide Scripts incrementieren automatisch die Patch-Version nach einem erfolgreichen Release:
- `1.0.1-SNAPSHOT` → Release `1.0.1` → nächste Development-Version `1.0.2-SNAPSHOT`

## Technische Details

### Maven-Profile Konfiguration (pom.xml)
```xml
<profile>
    <id>release</id>
    <properties>
        <release.version>${project.version}</release.version>
    </properties>
    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <version>3.4.0</version>
                <executions>
                    <execution>
                        <id>remove-snapshot</id>
                        <phase>initialize</phase>
                        <goals>
                            <goal>regex-property</goal>
                        </goals>
                        <configuration>
                            <name>release.version</name>
                            <value>${project.version}</value>
                            <regex>-SNAPSHOT</regex>
                            <replacement></replacement>
                            <failIfNoMatch>false</failIfNoMatch>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
        <resources>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>true</filtering>
            </resource>
        </resources>
    </build>
</profile>
```

### Application Properties
```properties
# Application Version (filtered by Maven)
quarkus.application.version=@release.version@
```

## Empfehlung

**Nutzen Sie Lösung 2 (Maven-Profile)** da sie:
- Robuster und weniger fehleranfällig ist
- Standard-Maven-Praktiken folgt
- Keine temporären Änderungen an der `pom.xml` vornimmt
- Besser in CI/CD-Pipelines integrierbar ist

## Test der Lösung

1. **Entwicklungstest:**
   ```bash
   ./mvnw quarkus:dev
   curl http://localhost:8080/csp/version
   # Sollte: {"version":"1.0.1-SNAPSHOT"} anzeigen
   ```

2. **Release-Test:**
   ```bash
   ./build_dockerfile_profile.sh --release
   docker run -p 8080:8080 grolimundachim/csp-report-api:1.0.1
   curl http://localhost:8080/csp/version
   # Sollte: {"version":"1.0.1"} anzeigen (ohne SNAPSHOT)