package org.example.fileserver;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileNameHelper {


    private static final int MAX_LOOKUP = 200;

    public static String getFileName(String path, String fileName) {

        Path actual = Paths.get(path, fileName).normalize();

        if (!actual.toFile().exists()) {
            return actual.toString();
        }

        String prefixName = removeExtension(fileName);
        String extension = getExtension(fileName);

        for (int i = 0; i < MAX_LOOKUP; i++) {
            String newFileName = String.format("%s(%d).%s", prefixName, i, extension);
            Path renamed = Paths.get(path, newFileName).normalize();

            if (!renamed.toFile().exists()) {
                return renamed.toString();
            }
        }

        return null;
    }

    public static String removeExtension(String filename) {
        if (filename == null) {
            return null;
        }

        int lastDotIndex = filename.lastIndexOf('.');

        // Ensure the dot isn't the first character (e.g., ".bashrc")
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(0, lastDotIndex);
        }

        return filename;
    }

    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }

        int lastDotIndex = filename.lastIndexOf('.');

        // Ensure the dot is not the first character and there is something after it
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }

        return ""; // No extension
    }

}
