package bside.keepon.coupon.service;

import bside.keepon.ResultType;
import bside.keepon.coupon.dto.Img.ImgUploadDto;
import bside.keepon.coupon.dto.Img.ImgUploadParamDto;
import bside.keepon.coupon.entity.Coupon;
import bside.keepon.coupon.entity.Image;
import bside.keepon.coupon.etc.CouponException;
import bside.keepon.coupon.repository.ImageJpaRepository;
import bside.keepon.coupon.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static bside.keepon.coupon.util.ImageUtil.getUriComponents;

@Service
@PropertySource("classpath:img.properties")
@Transactional
@Slf4j
class ImageSaveService implements ImageService<ImgUploadParamDto,ImgUploadDto> {

    private final String secretKey;

    private final String saveUploadUrl;



    private ImageJpaRepository imageJpaRepository;


    public ImageSaveService(@Value("${upload.secret}") String secretKey,
                            ImageJpaRepository imageJpaRepository,
                            @Value("${save.upload.url}") String saveUploadUrl
                            ){
        this.secretKey = secretKey;
        this.imageJpaRepository = imageJpaRepository;
        this.saveUploadUrl = saveUploadUrl;
    }

    /**
     * 이미지 서버에 업로드(단건)
     * @param
     * @param param
     * @throws IOException
     * @return
     */
    @Override
    public ImgUploadDto imageServerApi(ImgUploadParamDto param) throws IOException {
        ImgUploadDto imgUploadDto = new ImgUploadDto();
        //url 생성
        UriComponents uriComponents = getUriComponents( saveUploadUrl,"path=/keepon-image/"+param.getStoreFileNm(),"overwrite="+param.isOverwrite());
        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", secretKey);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        ResponseEntity<Map> response = rt.exchange(uriComponents.toUriString(), HttpMethod.PUT, new HttpEntity<>(param.getMultipartFile().getBytes(), headers), Map.class);
        log.debug("save header = {}",response.getBody().get("header"));

        Map<String, Object> result = ImageUtil.<ImgUploadDto>findBodyVal(response.getBody(), new HashMap<>());
        imgUploadDto.setImage(param.getMultipartFile());
        imgUploadDto.setServerId((String)result.get("id"));
        imgUploadDto.setSuccessful((boolean)result.get("isSuccessful"));
        imgUploadDto.setName((String)result.get("name"));
        imgUploadDto.setSize((int)result.get("bytes"));

        if (!imgUploadDto.isSuccessful()) {
            throw new CouponException(ResultType.fail_img_server_upload, "쿠폰 이미지 업로드 실패" + "(" + param.getMultipartFile().getOriginalFilename() + ")");
        }

        return imgUploadDto;
    }

    /**
     * 이미지 db 저장
     * @param imgUploadDto
     * @param coupon
     * @return
     */
    private Image createSaveImage(ImgUploadDto imgUploadDto, Coupon coupon) {
        //이미지 db저장
        Image image = Image.builder()
                .imgName(imgUploadDto.getName())
                .imgType(imgUploadDto.getImgType())
                .imgSize(imgUploadDto.getImage().getSize())
                .imgServerId(imgUploadDto.getServerId())
                .coupon(coupon)
                .regDtm(LocalDateTime.now())
                .build();
        imageJpaRepository.save(image);
        return image;
    }

}
