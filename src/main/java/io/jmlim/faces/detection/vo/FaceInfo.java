package io.jmlim.faces.detection.vo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FaceInfo {
    //faceCeritification
    public MultipartFile faceImg;  //얼굴사진 묶음
    //etc
    public String email;
}
