package bside.keepon.coupon.dto.find;

import bside.keepon.coupon.etc.ImgType;
import bside.keepon.coupon.etc.UseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetailCouponDto {

    private Long couponId;
    private String couponNo;
    private String couponNm;
    private String brandNm;
    private String expiredDate;
    private UseStatus useYn;
    private Map<ImgType, String> images;
}
