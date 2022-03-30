package bside.keepon.coupon.dto.save;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponUpdateDto {
    private Long couponId;
    private Optional<String> couponNm;
    private Optional<String> BrandNm;
    private Optional<String> expiredDate;
}
