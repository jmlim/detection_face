package io.jmlim.faces.detection.config;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class AwsFileManager {

    @Value("${aws.s3.bucket}")
    private String BUCKET_NAME;

    @Autowired
    private AmazonS3Client amazonS3Client;

    @Autowired
    private Environment environment;

    /**
     * @param file
     * @param path ex)/jmlimfile
     * @return String 업로드 된 경로
     */
    public String uploadFile(File file, String fileName, String path) {
        return uploadFile(BUCKET_NAME, file, fileName, path);
    }

    /**
     * @param bucket
     * @param file
     * @param fileName
     * @param path
     * @return
     */
    public String uploadFile(String bucket, File file, String fileName, String path) {
        String profile = "";
        for (String e : environment.getActiveProfiles()) {
            if (!"real".equals(e)) {
                profile = "/test";
            }
        }

        String storedPath = profile + path;

        try {
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucket + storedPath, fileName, file);
            putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead); // file permission
            PutObjectResult putObjectResult = amazonS3Client.putObject(putObjectRequest); // upload file

        } catch (AmazonServiceException ase) {
            log.debug("AmazonServiceException : ", ase);
        }

        return storedPath + "/" + fileName;
    }
}