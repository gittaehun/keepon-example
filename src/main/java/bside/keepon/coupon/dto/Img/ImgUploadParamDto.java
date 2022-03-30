package bside.keepon.coupon.dto.Img;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImgUploadParamDto {
    private String storeFileNm;
    private MultipartFile multipartFile;
    private boolean overwrite;
}
