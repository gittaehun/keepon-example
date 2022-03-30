package bside.keepon.coupon.dto.Img;

import bside.keepon.coupon.etc.ImgType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImgUploadDto {
    private boolean successful;
    private String serverId;
    private String name;
    private Integer size;
    private ImgType imgType;
    private MultipartFile image;
}
