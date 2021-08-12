package com.hidebush.roma.util.reporter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by htf on 2021/8/11.
 */
public class ConsoleReporter implements Reporter {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String roleDisplayName;

    public ConsoleReporter(String role, int id) {
        this.roleDisplayName = formatRole(role + "(" + id + ")");
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
        System.out.println(formatter.format(LocalDateTime.now()) + " [" + level + "] " + roleDisplayName + " " + msg);
    }

    private String formatRole(String name) {
        if (name.length() <= 20) {
            return String.format("%-25s", name);
        }
        if (name.length() <= 30) {
            return String.format("%-35s", name);
        }
        return name;
    }
}
