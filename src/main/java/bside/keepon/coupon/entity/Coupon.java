package bside.keepon.coupon.entity;

import bside.keepon.coupon.dto.Img.ImgUploadDto;
import bside.keepon.coupon.dto.save.CouponUpdateDto;
import bside.keepon.coupon.etc.ImgType;
import bside.keepon.coupon.etc.UseStatus;
import bside.keepon.crypto.CryptoConverter;
import bside.keepon.user.entity.Member;
import lombok.*;

import javax.persistence.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static javax.persistence.FetchType.LAZY;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@SequenceGenerator(
        name = "seq_coupon_gen",
        sequenceName = "seq_coupon",
        initialValue = 1,
        allocationSize = 1
)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_coupon_gen")
    @Column(name = "coupon_id")
    private Long id;

    //@Convert(converter= CryptoConverter.class)
    private String couponNo;

    private String couponNm;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Enumerated(EnumType.STRING)
    private UseStatus useYn;

    private String expDate;

    private LocalDateTime useDtm;

    private LocalDateTime regDtm;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name="mbr_id")
    private Member member;

    @OneToMany(mappedBy = "coupon", cascade ={CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Image> images;

    String decryptedText;

    public void setBrand(Brand brand) {
        this.brand = brand;
    }
    public void setImages(List<Image> images){this.images = images;}
    public void setMember(Member member) {
        this.member = member;
    }
    public void setThumbnailImage(ImgUploadDto image){
        Optional<Image> thumbnail = images.stream().filter(s -> s.getImgType().equals(ImgType.THUMBNAIL)).findFirst();
        if(thumbnail.isPresent()){
            thumbnail.get().updateImage(image);
        }
    }

    //==비즈니스 로직==//
    /**
     * 쿠폰 상태변경
     */
    public void updateUseStatus(UseStatus useStatus){
        this.useYn = useStatus;
        if (useYn == UseStatus.Y) updateUseDtm();
        else if (useDtm != null) useDtm = null;
    }

    public void updateCoupon(CouponUpdateDto dto){
        this.couponNm = dto.getCouponNm().isPresent() ? dto.getCouponNm().get() : this.getCouponNm();
        this.expDate = dto.getExpiredDate().isPresent() ? dto.getExpiredDate().get() : this.getExpDate();
    }

    public LocalDate strToDate(String strDate) throws ParseException {
        LocalDate date = LocalDate.parse(strDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        return date;
    }

    public void setCouponNo (String couponNo){
        this.couponNo = couponNo;
    }


    public void updateUseDtm() {
        useDtm = LocalDateTime.now();
    }
}