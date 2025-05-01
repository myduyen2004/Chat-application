package com.chat.util;

import javax.servlet.http.Part;
import java.io.File;
import java.util.UUID;

public class FileUtil {
    public static String getUniqueFileName(Part part) {
        String submittedFileName = part.getSubmittedFileName();
        String extension = submittedFileName.substring(submittedFileName.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }

    public static boolean ensureUploadDirExists(String path) {
        File uploadDir = new File(path);
        if (!uploadDir.exists()) {
            return uploadDir.mkdirs();
        }
        return true;
    }
}