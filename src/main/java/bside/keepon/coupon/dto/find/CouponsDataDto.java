package bside.keepon.coupon.dto.find;

import bside.keepon.coupon.etc.UseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponsDataDto {
    private Long couponId;
    private String couponNo;
    private String couponNm;
    private String thumbnailUrl;
    private String expiredDate;
    private UseStatus useYn;
}
