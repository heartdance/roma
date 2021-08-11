package com.hidebush.roma.util.reporter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by htf on 2021/8/11.
 */
public class ConsoleReporter implements Reporter {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String role;

    public ConsoleReporter(String role) {
        this.role = role;
    }

    @Override
    public void debug(String msg) {
        out("DEBUG", msg);
    }

    @Override
    public void info(String msg) {
        out("INFO ", msg);
    }

    @Override
    public void warn(String msg) {
        out("WARN ", msg);
    }

    @Override
    public void error(String msg) {
        out("ERROR", msg);
    }

    private void out(String level, String msg) {
        System.out.println(formatter.format(LocalDateTime.now()) + " [" + level + "] " + formatRole(role) + " " + msg);
    }

    private String formatRole(String role) {
        if (role.length() <= 20) {
            return String.format("%-25s", role);
        }
        if (role.length() <= 30) {
            return String.format("%-35s", role);
        }
        return role;
    }
}
