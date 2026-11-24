package com.college.finance.model;

import java.time.LocalDate;

public class RuleAlert {
    private final LocalDate date;
    private final String ruleName;
    private final String severity;
    private final String message;

    public RuleAlert(LocalDate date, String ruleName, String severity, String message) {
        this.date = date;
        this.ruleName = ruleName;
        this.severity = severity;
        this.message = message;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public String toLogLine() {
        return String.format("[%s] [%s] (%s) %s", date, severity, ruleName, message);
    }

    @Override
    public String toString() {
        return toLogLine();
    }
}