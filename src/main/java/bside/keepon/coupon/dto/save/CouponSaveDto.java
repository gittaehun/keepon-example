package bside.keepon.coupon.dto.save;

import bside.keepon.coupon.etc.ImgType;
import bside.keepon.user.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponSaveDto {

    private SaveRequestDataDto data;

    private Member member;

    private Map<ImgType, MultipartFile> images;

}
