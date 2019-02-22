package io.jmlim.faces.detection.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "FACE_ACCOUNT_TEST")
@Getter
@Setter
@ToString(callSuper = true)
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ACCOUNT_ID")
    private Long id;

    @Column(length = 50, unique = true, nullable = false)
    private String email;

    //성별
    @Enumerated(EnumType.STRING)
    private GenderType gender;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date joinDate;

    //face 이미지 경로(s3)
    @Column(length = 500)
    private String faceImgPath;

    //aws faceId
    @Column(length = 100)
    private String faceId;

    private String collectionId;

    public enum GenderType {
        MALE("남성"), FEMALE("여성");

        GenderType(String value) {
            this.value = value;
        }

        private String value;

        public String getKey() {
            return name();
        }

        public String getValue() {
            return value;
        }
    }
}