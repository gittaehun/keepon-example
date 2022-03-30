package bside.keepon.coupon.dto.find;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImminentCouponsDto {
    private String thumbnailUrl;
    private String brandNm;
    private String couponNm;
}
