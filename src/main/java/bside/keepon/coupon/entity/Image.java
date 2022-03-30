package bside.keepon.coupon.entity;

import bside.keepon.coupon.dto.Img.ImgUploadDto;
import bside.keepon.coupon.etc.ImgType;
import bside.keepon.user.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SequenceGenerator(
        name = "seq_image_gen",
        sequenceName = "seq_image",
        initialValue = 1,
        allocationSize = 1
)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_image_gen")
    @Column(name = "img_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private ImgType imgType;

    private String imgName;

    private Long imgSize;

    private String imgServerId;

    private LocalDateTime regDtm;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public void updateImage(ImgUploadDto image){
        this.imgName = image.getName();
        this.imgSize = Long.valueOf(image.getSize());
        this.imgServerId = image.getServerId();
        this.regDtm = LocalDateTime.now();
    }

}