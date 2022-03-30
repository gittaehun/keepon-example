package bside.keepon.coupon.dto.save;

import lombok.Data;

@Data
public class SaveRequestDataDto {
    private String couponNo;

    private String couponNm;

    private String brandNm;

    private String expiredDate;
}
