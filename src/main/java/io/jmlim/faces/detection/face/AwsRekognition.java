package io.jmlim.faces.detection.face;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.*;
import io.jmlim.faces.detection.face.exception.FaceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

@Component
@Slf4j
public class AwsRekognition {

    @Autowired
    private AmazonRekognition amazonRekognition;

    private final String MEMBER_FACE_COLLEACTION_NAME = "JmlimTestFaces";

    /**
     * 얼굴 컬렉션 생성.
     *
     * @param collectionId
     */
    public void createCollection(String collectionId) {
        CreateCollectionRequest request = new CreateCollectionRequest()
                .withCollectionId(MEMBER_FACE_COLLEACTION_NAME);

        CreateCollectionResult createCollectionResult = amazonRekognition.createCollection(request);
        log.debug("CollectionArn : " +
                createCollectionResult.getCollectionArn());
        log.debug("Status code : " +
                createCollectionResult.getStatusCode().toString());
    }

    /**
     * 컬렉션 리스트 가져오기.
     *
     * @return
     */
    public ListCollectionsResult listCollection() {
        int limit = 1;
        ListCollectionsResult listCollectionsResult = null;
        String paginationToken = null;
        do {
            if (listCollectionsResult != null) {
                paginationToken = listCollectionsResult.getNextToken();
            }
            ListCollectionsRequest listCollectionsRequest = new ListCollectionsRequest()
                    .withMaxResults(limit)
                    .withNextToken(paginationToken);
            listCollectionsResult = amazonRekognition.listCollections(listCollectionsRequest);

            List<String> collectionIds = listCollectionsResult.getCollectionIds();
            for (String resultId : collectionIds) {
                log.debug(resultId);
            }
        } while (listCollectionsResult != null && listCollectionsResult.getNextToken() != null);

        return listCollectionsResult;
    }

    /**
     * 컬렉션 삭제.
     *
     * @param collectionId
     */
    public void deleteCollection(String collectionId) {

        log.debug("Deleting collections");

        DeleteCollectionRequest request = new DeleteCollectionRequest()
                .withCollectionId(MEMBER_FACE_COLLEACTION_NAME);
        DeleteCollectionResult deleteCollectionResult = amazonRekognition.deleteCollection(request);

        log.debug(collectionId + ": " + deleteCollectionResult.getStatusCode()
                .toString());
    }

    /**
     * 페이스 체크
     */
    public DetectFacesResult checkFace(ByteBuffer imgBuffer) throws Exception {

        //face datail length가 얼굴 갯수이다.. 얼굴이 하나일때만 등록해야함
        DetectFacesResult detectFacesResult = amazonRekognition.detectFaces(new DetectFacesRequest()
                .withImage(new Image().withBytes(imgBuffer)));

        Pose pose = detectFacesResult.getFaceDetails().get(0).getPose();

        //TODO 테스트 후 삭제
        log.debug(detectFacesResult.toString());
        log.debug("Pitch :: " + pose.getPitch());
        log.debug("Roll :: " + pose.getRoll());
        log.debug("Yaw :: " + pose.getYaw());


        //얼굴을 찾을 수 없음
        if (detectFacesResult.getFaceDetails().size() == 0) {
            throw new FaceException(1, "얼굴을 찾을 수 없음");
        }

        //얼굴이 하나가 아님
        /*if(detectFacesResult.getFaceDetails().size() > 1) {
            throw new FaceException(2, "얼굴이 2개이상임");
        }*/

        //얼굴 인식률이 90이 안넘음
        if (detectFacesResult.getFaceDetails().get(0).getConfidence() < 90) {
            throw new FaceException(3, "얼굴이 정확치 않음");
        }

/*        //선글라스 착용
        if(detectFacesResult.getFaceDetails().get(0).getSunglasses().getValue()) {
            throw new FaceException(4, "선글라스 착용 상태");
        }*/

        /**
         * PITCH : 옆에서 봤을때 회전 값
         * ROLL : 정면에서 봤을때 회전 값
         * YAW : 위에서 봤을때 회전 값
         */
        long PITCH_MAX = 35;
        long PITCH_MIN = -12;
        long ROLL_MAX = 35;
        long ROLL_MIN = -35;
        long YAW_MAX = 25;
        long YAW_MIN = -25;

        //Pose pose = detectFacesResult.getFaceDetails().get(0).getPose();
        if (pose.getPitch() < PITCH_MIN
                || pose.getPitch() > PITCH_MAX
                || pose.getRoll() < ROLL_MIN
                || pose.getRoll() > ROLL_MAX
                || pose.getYaw() < YAW_MIN
                || pose.getYaw() > YAW_MAX) {
            throw new FaceException(0005, "얼굴이 정면이 아님", detectFacesResult);
        }

        return detectFacesResult;
    }

    /**
     * 컬렉션에 얼굴을 추가.
     *
     * @param imgBuffer
     * @param imgId
     * @return
     * @throws Exception
     */
    public String addFace(ByteBuffer imgBuffer, String imgId) throws Exception {

        //이미지 체크한다
        checkFace(imgBuffer);

        //바이트로 전송
        Image image = new Image().withBytes(imgBuffer);

        IndexFacesRequest indexFacesRequest = new IndexFacesRequest()
                .withImage(image)
                .withCollectionId(MEMBER_FACE_COLLEACTION_NAME)
                .withExternalImageId(imgId)
                .withDetectionAttributes("ALL");

        IndexFacesResult indexFacesResult = amazonRekognition.indexFaces(indexFacesRequest);

        log.debug(imgId + " added");
        List<FaceRecord> faceRecords = indexFacesResult.getFaceRecords();

        //얼굴이 1개가 아니라면 위쪽에서 exception이 떨어짐. 얼굴은 무조건 1개
        return faceRecords.get(0).getFace().getFaceId();
    }

    /**
     * 얼굴 이미지를 컬렉션에서 찾음.
     *
     * @param imgBuffer
     * @return
     * @throws Exception
     */
    public List<FaceMatch> findFace(ByteBuffer imgBuffer) throws Exception {

        //이미지 체크한다
        checkFace(imgBuffer);

        //바이트로 바로 전송.
        Image image = new Image().withBytes(imgBuffer);
                /*.withS3Object(new S3Object()
                        .withBucket(BUCKET_NAME)
                        .withName(faceImg))*/
        ;

        // Search collection for faces similar to the largest face in the image.
        SearchFacesByImageRequest searchFacesByImageRequest = new SearchFacesByImageRequest()
                .withCollectionId(MEMBER_FACE_COLLEACTION_NAME)
                .withImage(image)
                .withFaceMatchThreshold(90F) //88이상 매치 되는넘만 가져온다
                .withMaxFaces(1);

        SearchFacesByImageResult searchFacesByImageResult =
                amazonRekognition.searchFacesByImage(searchFacesByImageRequest);

        return searchFacesByImageResult.getFaceMatches();
    }

    /**
     * 컬렉션에 들어있는 리스트를 가져온다.
     *
     * @param collectionId
     * @return
     * @throws Exception
     */
    public ListFacesResult findFaceList(String collectionId) throws Exception {
        int limit = 10;
        ListFacesResult listFacesResult = null;
        String paginationToken = null;
        do {
            if (listFacesResult != null) {
                paginationToken = listFacesResult.getNextToken();
            }
            ListFacesRequest listFacesRequest = new ListFacesRequest()
                    .withCollectionId(collectionId)
                    .withMaxResults(limit)
                    .withNextToken(paginationToken);
            listFacesResult = amazonRekognition.listFaces(listFacesRequest);
            log.debug("리스트 : " + listFacesResult.toString());
        } while (listFacesResult != null && listFacesResult.getNextToken() != null);

        return listFacesResult;
    }

    /**
     * 얼굴 삭제.
     *
     * @param collectionId
     * @param faceId
     * @throws Exception
     */
    public void delFace(String collectionId, String faceId) throws Exception {
        DeleteFacesRequest deleteFacesRequest = new DeleteFacesRequest()
                .withCollectionId(StringUtils.isEmpty(collectionId) ? MEMBER_FACE_COLLEACTION_NAME : collectionId)
                .withFaceIds(faceId);
        amazonRekognition.deleteFaces(deleteFacesRequest);
    }
}