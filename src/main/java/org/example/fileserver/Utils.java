package org.example.fileserver;

public class Utils {

    public static String getWebDirectory() {
        return getFromSystemPropertyOrDefault("uploads.dir", "web");
    }

    public static String getClipboardDirectory() {
        return getFromSystemPropertyOrDefault("clipboard.dir", "clipboard");
    }

    public static String getUserAuthFilePath() {
        return getFromSystemPropertyOrDefault("auth.file", "auth.conf");
    }

    public static String getConfigDirectory() {
        return getFromSystemPropertyOrDefault("conf.dir", "conf");
    }

    public static String getTemporaryUploadsDirectory() {
        return getFromSystemPropertyOrDefault("upload.tmp.dir", "uploads");
    }

    public static boolean enableSsl() {
        String enableSsl = getFromSystemPropertyOrDefault("enable.server.ssl", "true");
        return Boolean.parseBoolean(enableSsl);
    }

    public static int getPort() {
        String portStr = getFromSystemPropertyOrDefault("server.port", "8080");
        return Integer.parseInt(portStr);
    }

    public static String getPfxFilePath() {
        return getFromSystemPropertyOrDefault("server.ssl.pfx", "service.pfx");
    }

    public static String getPfxPasswordFile() {
        return getFromSystemPropertyOrDefault("server.ssl.pfx.password.file", "changeit");
    }

    public static String getFromSystemPropertyOrDefault(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
}
