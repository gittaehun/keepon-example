package bside.keepon.coupon.dto.Img;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImgDeleteDto {
    private boolean isSuccessful;
    private String resultMessage;
    private int resultCode;
}
