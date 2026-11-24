package com.college.finance.persistence;

import com.college.finance.exception.InvalidTransactionException;
import com.college.finance.model.Account;
import com.college.finance.model.ExpenseTransaction;
import com.college.finance.model.IncomeTransaction;
import com.college.finance.model.RuleAlert;
import com.college.finance.model.Transaction;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileStorageManager {
    private final Path dataDir;
    private final Path stateFile;
    private final Path alertLogFile;

    public FileStorageManager(String directoryPath) {
        this.dataDir = Path.of(directoryPath);
        this.stateFile = this.dataDir.resolve("ledger_state.csv");
        this.alertLogFile = this.dataDir.resolve("alerts.log");
    }

    public boolean hasSavedState() {
        return Files.exists(stateFile) && Files.isRegularFile(stateFile);
    }

    public void saveLedger(Account account) throws IOException {
        Files.createDirectories(dataDir);
        List<String> lines = new ArrayList<>();
        lines.add("RECORD_TYPE,COL1,COL2,COL3,COL4,COL5,COL6");
        lines.add(String.format("ACCOUNT,%s,%.2f,,,,",
                escape(account.getOwnerName()), account.getBalance()));

        for (Map.Entry<String, Double> entry : account.getCategoryBudgets().entrySet()) {
            lines.add(String.format("BUDGET,%s,%.2f,,,,",
                    escape(entry.getKey()), entry.getValue()));
        }

        for (Transaction tx : account.getTransactions()) {
            lines.add(String.format("TX,%s,%s,%.2f,%s,%s,%s",
                    escape(tx.getId()),
                    tx.getType().name(),
                    tx.getAmount(),
                    escape(tx.getCategory()),
                    tx.getDate().toString(),
                    escape(tx.getNote())));
        }

        Files.write(stateFile, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public Account loadLedger() throws IOException, InvalidTransactionException {
        if (!hasSavedState()) {
            throw new IOException("No saved ledger file found at " + stateFile);
        }

        String ownerName = null;
        double initialBalance = 0.0;
        List<String[]> budgetRows = new ArrayList<>();
        List<String[]> txRows = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(stateFile, StandardCharsets.UTF_8)) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length < 7) {
                    throw new InvalidTransactionException("Malformed CSV line: " + line);
                }
                String recordType = parts[0].trim();
                if ("ACCOUNT".equals(recordType)) {
                    ownerName = parts[1].trim();
                    initialBalance = Double.parseDouble(parts[2].trim());
                } else if ("BUDGET".equals(recordType)) {
                    budgetRows.add(parts);
                } else if ("TX".equals(recordType)) {
                    txRows.add(parts);
                }
            }
        }

        if (ownerName == null) {
            throw new InvalidTransactionException("Missing ACCOUNT record in " + stateFile);
        }

        Account account = new Account(ownerName, 0.0);
        for (String[] b : budgetRows) {
            account.setCategoryBudget(b[1].trim(), Double.parseDouble(b[2].trim()));
        }

        for (String[] t : txRows) {
            String id = t[1].trim();
            String type = t[2].trim();
            double amount = Double.parseDouble(t[3].trim());
            String category = t[4].trim();
            LocalDate date = LocalDate.parse(t[5].trim());
            String note = t[6].trim();

            Transaction tx;
            if ("INCOME".equals(type)) {
                tx = new IncomeTransaction(id, amount, category, date, note);
            } else if ("EXPENSE".equals(type)) {
                tx = new ExpenseTransaction(id, amount, category, date, note);
            } else {
                throw new InvalidTransactionException("Unknown transaction type: " + type);
            }

            try {
                account.addTransaction(tx);
            } catch (Exception e) {
                throw new InvalidTransactionException("Failed replaying transaction " + id + ": " + e.getMessage());
            }
        }

        if (Math.abs(account.getBalance() - initialBalance) > 0.009 && txRows.isEmpty()) {
            account = new Account(ownerName, initialBalance);
            for (String[] b : budgetRows) {
                account.setCategoryBudget(b[1].trim(), Double.parseDouble(b[2].trim()));
            }
        }

        return account;
    }

    public void appendAlerts(List<RuleAlert> alerts) throws IOException {
        if (alerts.isEmpty()) return;
        Files.createDirectories(dataDir);
        try (BufferedWriter writer = Files.newBufferedWriter(alertLogFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            for (RuleAlert alert : alerts) {
                writer.write(alert.toLogLine());
                writer.newLine();
            }
        }
    }

    public Path getStateFilePath() {
        return stateFile;
    }

    public Path getAlertLogFilePath() {
        return alertLogFile;
    }

    private String escape(String value) {
        return value == null ? "" : value.replace(",", ";").trim();
    }
}