package com.honeywell.controller;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileUploadController {

	@Value("${file.upload-dir}")
    private String uploadDirPath;
    

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws Exception {
    	Path uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        model.addAttribute("files", Files.walk(uploadDir, 1)
                .filter(path -> !path.equals(uploadDir))
                .map(uploadDir::relativize)
                .collect(Collectors.toList()));

        return "upload";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
    	Path uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        try {
            Path destinationFile = uploadDir.resolve(
                    Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(uploadDir.toAbsolutePath())) {
                throw new IllegalStateException("Cannot store file outside current directory.");
            }
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            model.addAttribute("message", "File uploaded successfully: " + file.getOriginalFilename());
        } catch (Exception e) {
            model.addAttribute("message", "Failed to upload file: " + e.getMessage());
        }
        return "upload";
    }
    
    @PostMapping("/sign")
    public String signFile(Model model) {
        try {
        	Path uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        	
            // Assume the latest uploaded file is the one to be signed
            Path latestFile = Files.list(uploadDir)
                    .filter(Files::isRegularFile)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No files found to sign"));
            
            // Read the private key (for demonstration, this should be securely stored)
            String privateKeyPEM = "MIIBOQIBAAJAXWRPQyGlEY+SXz8Uslhe+MLjTgWd8lf/nA0hgCm9JFKC1tq1S73cQ9naClNXsMqY7pwPt1bSY8jYRqHHbdoUvwIDAQABAkAfJkz1pCwtfkig8iZSEf2jVUWBiYgUA9vizdJlsAZBLceLrdk8RZF2YOYCWHrpUtZVea37dzZJe99Dr53K0UZxAiEAtyHQBGoCVHfzPM//a+4tv2ba3tx9at+3uzGR86YNMzcCIQCCjWHcLW/+sQTWOXeXRrtxqHPp28ir8AVYuNX0nT1+uQIgJm158PMtufvRlpkux78a6mby1oD98Ecxjp5AOhhF/NECICyHsQN69CJ5mt6/R01wMOt5u9/eubn76rbyhPgk0h7xAiEAjn6mEmLwkIYD9VnZfp9+2UoWSh0qZiTIHyNwFpJH78o=";
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", ""));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // Sign the file
            byte[] fileBytes = Files.readAllBytes(latestFile);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(fileBytes);
            byte[] digitalSignature = signature.sign();

            // Save the signed file
            Path signedFile = uploadDir.resolve(latestFile.getFileName().toString() + ".signed");
            try (FileOutputStream fos = new FileOutputStream(signedFile.toFile())) {
                fos.write(digitalSignature);
            }

            model.addAttribute("signedFileName", signedFile.getFileName().toString());
            model.addAttribute("message", "File signed successfully: " + signedFile.getFileName().toString());
        } catch (Exception e) {
        	e.printStackTrace();
            model.addAttribute("message", "Failed to sign file: " + e.getMessage());
        }
        return "upload";
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws Exception {
    	Path uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
    	Path file = uploadDir.resolve(filename);
        Resource resource = new UrlResource(file.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
