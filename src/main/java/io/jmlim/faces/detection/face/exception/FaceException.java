package io.jmlim.faces.detection.face.exception;

import com.amazonaws.services.rekognition.model.DetectFacesResult;
import com.amazonaws.services.rekognition.model.Pose;

import java.util.HashMap;
import java.util.Map;

public class FaceException extends Exception {

    public int code = 0;

    public Map<String, Object> data = new HashMap<>();

    public FaceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public FaceException(int code, String message, DetectFacesResult detectFacesResult) {
        super(message);
        Pose pose = detectFacesResult.getFaceDetails().get(0).getPose();
        this.code = code;
        this.data.put("pitch", detectFacesResult.getFaceDetails().get(0).getPose().getPitch());
        this.data.put("roll", detectFacesResult.getFaceDetails().get(0).getPose().getRoll());
        this.data.put("yaw", pose.getYaw());
    }
}