package com.yb.forum.utils;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;

@Slf4j
@Component
public class MinioUtil {

    @Resource
    private MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 上传文件到 MinIO
     * @param file 上传的文件
     * @return 返回文件的访问完整 URL
     */
    public String uploadAvatar(MultipartFile file) throws Exception {
        // 生成唯一的文件名，防止重名覆盖
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUIDUtil.UUID_32() + extension;

        InputStream inputStream = file.getInputStream();

        // 执行上传
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(newFileName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        // 拼接返回外链的 URL
        return endpoint + "/" + bucketName + "/" + newFileName;
    }
}