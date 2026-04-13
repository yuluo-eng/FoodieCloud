package com.example.springbootblank.upload.controller;

import com.example.springbootblank.auth.security.MerchantAuthGuard;
import com.example.springbootblank.common.api.ApiResponse;
import com.example.springbootblank.common.error.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/merchant/upload")
public class UploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final MerchantAuthGuard merchantAuthGuard;

    public UploadController(MerchantAuthGuard merchantAuthGuard) {
        this.merchantAuthGuard = merchantAuthGuard;
    }

    @PostMapping("/image")
    public ApiResponse<Map<String, Object>> uploadImage(
            @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestPart("file") MultipartFile file
    ) {
        merchantAuthGuard.requireEmployeeRole(authorization, "SUPER_ADMIN", "SHOP_MANAGER", "STAFF");
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择图片文件");
        }

        String original = file.getOriginalFilename();
        String ext = getExtension(original);
        if (!isImageExt(ext)) {
            throw new BusinessException(400, "仅支持 jpg/jpeg/png/webp/gif/svg 图片");
        }

        try {
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(basePath);

            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = basePath.resolve(filename).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String relativeUrl = "/uploads/" + filename;
            String fullUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(relativeUrl)
                    .toUriString();

            return ApiResponse.ok(Map.of(
                    "url", fullUrl,
                    "relativeUrl", relativeUrl,
                    "filename", filename
            ));
        } catch (IOException e) {
            throw new BusinessException(500, "图片上传失败");
        }
    }

    private static String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean isImageExt(String ext) {
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("webp") || ext.equals("gif") || ext.equals("svg");
    }
}
