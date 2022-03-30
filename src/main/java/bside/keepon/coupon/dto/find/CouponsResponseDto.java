package bside.keepon.coupon.dto.find;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponsResponseDto {
    private String brand;
    private String brandImgUrl;
    private List<CouponsDataDto> couponData;

}
