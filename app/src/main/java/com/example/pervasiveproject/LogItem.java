package com.example.pervasiveproject;

public class LogItem {
    private final String code;
    private final String result;
    private final long timestampSeconds; // seconds since boot or epoch

    public LogItem(String code, String result, long timestampSeconds) {
        this.code = code;
        this.result = result;
        this.timestampSeconds = timestampSeconds;
    }

    public String getCode() {
        return code;
    }

    public String getResult() {
        return result;
    }

    public long getTimestampSeconds() {
        return timestampSeconds;
    }

    public String getFormattedTime() {
        // your ESP sends millis()/1000 => seconds since boot
        // we'll still render it as a time-of-day for readability
        long millis = timestampSeconds * 1000L;
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(millis));
    }
}

