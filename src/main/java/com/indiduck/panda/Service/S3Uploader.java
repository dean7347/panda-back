package com.indiduck.panda.Service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.indiduck.panda.util.MD5Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${bucketyname}")
    public String bucket;  // S3 버킷 이름

    public String upload(String path,MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID()+file.getOriginalFilename();


        amazonS3Client.putObject(new PutObjectRequest(bucket, path+"/"+fileName, file.getInputStream(), null)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return path+"/"+fileName;
    }

    public void delete(String path) {
        amazonS3Client.deleteObject(bucket, path);
    }
}


/**
 *     public String upload(MultipartFile multipartFile, String dirName) throws IOException {
 *         File uploadFile = convert(multipartFile)  // 파일 변환할 수 없으면 에러
 *                 .orElseThrow(() -> new IllegalArgumentException("error: MultipartFile -> File convert fail"));
 *
 *         return upload(uploadFile, dirName);
 *     }
 *
 *     // S3로 파일 업로드하기
 *     private String upload(File uploadFile, String dirName) {
 *         String fileName = dirName + "/" + UUID.randomUUID() + uploadFile.getName();   // S3에 저장된 파일 이름
 *         String uploadImageUrl = putS3(uploadFile, fileName); // s3로 업로드
 *         removeNewFile(uploadFile);
 *         return fileName;
 *     }
 *
 *     // S3로 업로드
 *     private String putS3(File uploadFile, String fileName) {
 *         amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
 *         return amazonS3Client.getUrl(bucket, fileName).toString();
 *     }
 *
 *     // 로컬에 저장된 이미지 지우기
 *     private void removeNewFile(File targetFile) {
 *         if (targetFile.delete()) {
 *             log.info("File delete success");
 *             return;
 *         }
 *         log.info("File delete fail");
 *     }
 *
 *     // 로컬에 파일 업로드 하기
 *     private Optional<File> convert(MultipartFile file) throws IOException {
 *         File convertFile = new File( "/uploads/" + file.getOriginalFilename());
 *         if (convertFile.createNewFile()) { // 바로 위에서 지정한 경로에 File이 생성됨 (경로가 잘못되었다면 생성 불가능)
 *             try (FileOutputStream fos = new FileOutputStream(convertFile)) { // FileOutputStream 데이터를 파일에 바이트 스트림으로 저장하기 위함
 *                 fos.write(file.getBytes());
 *             }
 *             return Optional.of(convertFile);
 *         }
 *
 *         return Optional.empty();
 *     }
 **/