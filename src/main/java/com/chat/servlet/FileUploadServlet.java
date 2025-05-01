package com.chat.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import javax.json.Json;
import javax.json.JsonObject;

@WebServlet("/upload")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1 MB
        maxFileSize = 1024 * 1024 * 10,  // 10 MB
        maxRequestSize = 1024 * 1024 * 15 // 15 MB
)
public class FileUploadServlet extends HttpServlet {

    private static final String UPLOAD_DIRECTORY = "uploads";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Tạo thư mục uploads nếu chưa tồn tại
        String applicationPath = request.getServletContext().getRealPath("");
        String uploadPath = applicationPath + File.separator + UPLOAD_DIRECTORY;

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        try {
            Part filePart = request.getPart("file");
            String fileName = getSubmittedFileName(filePart);
            String fileExtension = "";

            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                fileExtension = fileName.substring(i);
            }

            // Tạo tên file ngẫu nhiên để tránh trùng lặp
            String randomFileName = UUID.randomUUID().toString() + fileExtension;

            String filePath = uploadPath + File.separator + randomFileName;
            filePart.write(filePath);

            // Tạo URL tương đối cho file đã upload
            String relativeFilePath = request.getContextPath() + "/" + UPLOAD_DIRECTORY + "/" + randomFileName;

            // Trả về URL của file dưới dạng JSON
            response.setContentType("application/json");
            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("success", true)
                    .add("fileUrl", relativeFilePath)
                    .add("fileName", fileName)
                    .build();

            response.getWriter().print(jsonResponse.toString());

        } catch (Exception e) {
            response.setContentType("application/json");
            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("success", false)
                    .add("error", e.getMessage())
                    .build();

            response.getWriter().print(jsonResponse.toString());
        }
    }

    private String getSubmittedFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return fileName.substring(fileName.lastIndexOf('/') + 1)
                        .substring(fileName.lastIndexOf('\\') + 1);
            }
        }
        return null;
    }
}