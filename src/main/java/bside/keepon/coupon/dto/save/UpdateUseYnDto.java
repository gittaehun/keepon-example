package bside.keepon.coupon.dto.save;

import bside.keepon.coupon.etc.UseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUseYnDto {

    private Long couponId;
    private UseStatus useStatus;
}
