package com.agristock.service;

import com.agristock.model.AgriException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class PersistenceManager {
    private static final Path BACKUP_DIR = Persistence.FILE.getParent().resolve("backups");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final AgriStockSystem sys;

    public PersistenceManager(AgriStockSystem sys) {
        this.sys = sys;
    }

    public boolean hasSavedState() {
        return Files.exists(Persistence.FILE);
    }

    public boolean loadSystemState() {
        if (!hasSavedState()) {
            System.out.println("No existing persistence file found. Starting fresh state.");
            return false;
        }
        Persistence.load(sys);
        System.out.println("Active system state restored on startup.");
        return true;
    }


    public void saveSystemState() {
        backupCurrentFile();
        Persistence.save(sys);
        System.out.println("System state successfully persisted to " + Persistence.FILE);
    }

    private void backupCurrentFile() {
        if (!hasSavedState()) return;
        try {
            Files.createDirectories(BACKUP_DIR);
            Path backup = BACKUP_DIR.resolve("agristock_state_" + LocalDateTime.now().format(STAMP) + ".csv");
            Files.copy(Persistence.FILE, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            // A failed backup should not stop the save itself.
            System.out.println("Warning: could not back up the state file: " + ex.getMessage());
        }
    }

    public void restoreLatestBackup() {
        try {
            if (!Files.isDirectory(BACKUP_DIR)) throw new AgriException("No backups found.");
            Path latest = null;
            try (var files = Files.list(BACKUP_DIR)) {
                latest = files.filter(p -> p.getFileName().toString().endsWith(".csv"))
                        .max((a, b) -> a.getFileName().toString().compareTo(b.getFileName().toString()))
                        .orElse(null);
            }
            if (latest == null) throw new AgriException("No backups found.");
            Files.copy(latest, Persistence.FILE, StandardCopyOption.REPLACE_EXISTING);
            Persistence.load(sys);
        } catch (IOException ex) {
            throw new AgriException("Could not restore backup: " + ex.getMessage());
        }
    }
}