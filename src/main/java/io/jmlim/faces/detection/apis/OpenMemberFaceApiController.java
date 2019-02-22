package io.jmlim.faces.detection.apis;

import com.amazonaws.services.rekognition.model.FaceMatch;
import com.amazonaws.util.IOUtils;
import io.jmlim.faces.detection.face.AwsRekognition;
import io.jmlim.faces.detection.face.exception.FaceException;
import io.jmlim.faces.detection.model.Account;
import io.jmlim.faces.detection.model.object.api.ApiResponse;
import io.jmlim.faces.detection.repo.AccountRepository;
import io.jmlim.faces.detection.vo.FaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.ByteBuffer;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/open/member/face")
public class OpenMemberFaceApiController {

    private final AwsRekognition awsRekognition;
    private final AccountRepository repository;

    public OpenMemberFaceApiController(AwsRekognition awsRekognition, AccountRepository repository) {
        this.awsRekognition = awsRekognition;
        this.repository = repository;
    }

    /**
     * 얼굴 이미지로 멤버를 찾는다
     *
     * @param info
     * @return
     */
    @CrossOrigin
    @PostMapping("find")
    public ResponseEntity find(FaceInfo info) {
        try {
            List result = new ArrayList();
            List<Map<String, Object>> faceInfo = new ArrayList<>();
            //얼굴 리스트 가져옴
            String img = Base64.getEncoder().encodeToString(IOUtils.toByteArray(info.faceImg.getInputStream()));
            byte[] imgByte = Base64.getDecoder().decode(img.getBytes("UTF-8"));

            List<FaceMatch> faces = awsRekognition.findFace(ByteBuffer.wrap(imgByte));
            if (faces.size() == 0) {
                return ResponseEntity.ok(new ApiResponse(HttpStatus.SC_BAD_REQUEST,
                        "AWS에서 얼굴을 못찾음", "0001"));
            }

            List<String> faceIds = new ArrayList<>();
            for (FaceMatch faceMatch : faces) {
                faceIds.add(faceMatch.getFace().getFaceId());

                Map<String, Object> data = new HashMap<>();
                data.put("similarity", faceMatch.getSimilarity());
                data.put("width", faceMatch.getFace().getBoundingBox().getWidth());
                data.put("height", faceMatch.getFace().getBoundingBox().getHeight());
                data.put("left", faceMatch.getFace().getBoundingBox().getLeft());
                data.put("top", faceMatch.getFace().getBoundingBox().getTop());
                data.put("confidence", faceMatch.getFace().getConfidence());
                faceInfo.add(data);
            }

            Account account = repository.findByFaceId(faceIds.get(0));

            //검색된 사용자 없음
            if (account == null) {
                result.addAll(faceInfo);
                return ResponseEntity.ok(new ApiResponse(HttpStatus.SC_BAD_REQUEST,
                        "얼굴로 검색된 회원정보가 없음", "0002") {{
                    this.setData(result);
                }});
            }

            result.add(account);
            result.addAll(faceInfo);

            return ResponseEntity.ok(new ApiResponse() {{
                this.setData(result);
            }});

        } catch (FaceException fe) {
            log.debug("FaceException : ", fe);
            if (fe.code == 5) {
                log.debug(fe.data.toString());
                return ResponseEntity.ok(new ApiResponse(HttpStatus.SC_BAD_REQUEST,
                        fe.getMessage(), "0005") {{
                    this.setData(fe.data);
                }});
            } else {
                return ResponseEntity.ok(new ApiResponse(HttpStatus.SC_BAD_REQUEST,
                        fe.getMessage(), "0010"));
            }
        } catch (Exception e) {
            log.debug("Exception : ", e);
            return ResponseEntity.ok(new ApiResponse(HttpStatus.SC_BAD_REQUEST,
                    e.getMessage(), "0010"));
        }
    }
}