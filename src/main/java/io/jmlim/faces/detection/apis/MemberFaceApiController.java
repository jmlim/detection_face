package io.jmlim.faces.detection.apis;

import com.amazonaws.util.IOUtils;
import io.jmlim.faces.detection.config.AwsFileManager;
import io.jmlim.faces.detection.face.AwsRekognition;
import io.jmlim.faces.detection.face.exception.FaceException;
import io.jmlim.faces.detection.model.Account;
import io.jmlim.faces.detection.model.object.api.ApiResponse;
import io.jmlim.faces.detection.repo.AccountRepository;
import io.jmlim.faces.detection.vo.FaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/member/face")
public class MemberFaceApiController {

    @Autowired
    private AwsRekognition awsRekognition;

    @Autowired
    private AwsFileManager awsFileManager;

    @Autowired
    private AccountRepository repository;

    /**
     * 페이스이미지 등록테스트.
     */
    @PostMapping("enroll")
    public ResponseEntity enroll(FaceInfo faceInfo) {
        try {
            Account account = repository.findByEmail(faceInfo.getEmail());
            String img = Base64.getEncoder().encodeToString(IOUtils.toByteArray(faceInfo.faceImg.getInputStream()));
            String extension = FilenameUtils.getExtension(faceInfo.faceImg.getOriginalFilename());

            byte[] imgByte = Base64.getDecoder().decode(img.getBytes("UTF-8"));
            log.debug("디코드 완료");

            //맴버가 페이스id등록되어 있을경우 먼저 삭제 해야 한다
            if (StringUtils.isNotEmpty(account.getFaceId())) {
                awsRekognition.delFace("", account.getFaceId());
            }

            log.debug("페이스 삭제완료");

            String faceId = awsRekognition.addFace(ByteBuffer.wrap(imgByte), String.valueOf(account.getId()));
            log.debug("페이스 등록완료");

            //s3에 저장한다
            File convFile = File.createTempFile("JMLIMFAce", "JMLIM");
            FileUtils.writeByteArrayToFile(convFile, imgByte);
            String faceImgPath = awsFileManager.uploadFile(convFile, faceInfo.getEmail() + "_" + String.valueOf(new Date().getTime()) + "." + extension, "/faces");
            log.debug("페이스 이미지 s3 저장 완료");

            //member update 친다
            account.setFaceImgPath(faceImgPath);
            account.setFaceId(faceId);
            repository.save(account);

            log.debug("faceImgPath : " + faceImgPath);
            log.debug("faceId : " + faceId);

            return ResponseEntity.ok(new ApiResponse() {{
                this.setData(faceImgPath);
            }});

        } catch (FaceException fe) {
            log.debug("FaceException : ", fe);
            if (fe.code == 5) {
                return ResponseEntity.ok(new ApiResponse(HttpStatus.SC_BAD_REQUEST,
                        fe.getMessage(), "0005"));
            } else {
                return ResponseEntity.ok(new ApiResponse(HttpStatus.SC_BAD_REQUEST,
                        fe.getMessage(), "0002"));
            }

        } catch (Exception e) {
            log.debug("Exception : ", e);
            return ResponseEntity.ok(new ApiResponse(
                    HttpStatus.SC_BAD_REQUEST,
                    e.getMessage(), "0002"));
        }
    }

    /**
     *  페이스이미지 등록한다. 테스트용...
     */
    @PostMapping("collection")
    public ResponseEntity createCollection() {
        try {
            awsRekognition.createCollection("");
            return ResponseEntity.ok(new ApiResponse() {{
            }});
        } catch (Exception e) {
            log.error("collection Exception", e);
            return ResponseEntity.ok(new ApiResponse(
                    HttpStatus.SC_BAD_REQUEST,
                    e.getMessage(), "0002"));
        }
    }

    //페이스이미지 등록한다. 테스트용...
    @DeleteMapping("collection")
    public ResponseEntity deleteCollection() {
        try {
            awsRekognition.deleteCollection("");
            return ResponseEntity.ok(new ApiResponse() {{
            }});
        } catch (Exception e) {
            log.error("delete Collection Exception", e);
            return ResponseEntity.ok(new ApiResponse(
                    HttpStatus.SC_BAD_REQUEST,
                    e.getMessage(), "0002"));
        }
    }
}
