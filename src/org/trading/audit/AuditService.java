package org.trading.audit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class AuditService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static AuditService instance;

    private final Path auditFile;

    private AuditService() {
        this.auditFile = Path.of("data", "audit.csv");
        createAuditDirectory();
    }

    public static synchronized AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    public synchronized void logAction(String actionName) {
        try {
            boolean shouldWriteHeader = !Files.exists(auditFile) || Files.size(auditFile) == 0;
            if (shouldWriteHeader) {
                Files.writeString(
                        auditFile,
                        "nume_actiune,timestamp" + System.lineSeparator(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            }

            String line = escapeCsv(actionName) + "," + LocalDateTime.now().format(FORMATTER) + System.lineSeparator();
            Files.writeString(auditFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Nu s-a putut scrie in fisierul de audit: " + e.getMessage());
        }
    }

    private void createAuditDirectory() {
        try {
            Files.createDirectories(auditFile.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Nu s-a putut crea directorul pentru audit.", e);
        }
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
