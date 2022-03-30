package bside.keepon.coupon.service;

import bside.keepon.ResultType;
import bside.keepon.coupon.dto.Img.ImgDeleteDto;
import bside.keepon.coupon.entity.Image;
import bside.keepon.coupon.etc.CouponException;
import bside.keepon.coupon.repository.ImageJpaRepository;
import bside.keepon.coupon.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bside.keepon.coupon.util.ImageUtil.getUriComponents;

@Service
@PropertySource("classpath:img.properties")
@Transactional
@Slf4j
public class ImageDeleteService implements ImageService<List<Image>, ImgDeleteDto>{

    private final String secretKey;

    private ImageJpaRepository imageJpaRepository;

    private final String deleteUploadUrlMulti;


    public ImageDeleteService(@Value("${delete.upload.url.multi}") String deleteUploadUrlMulti,
                            @Value("${upload.secret}") String secretKey,
                              ImageJpaRepository imageJpaRepository
                            ){

        this.deleteUploadUrlMulti = deleteUploadUrlMulti;
        this.secretKey = secretKey;
        this.imageJpaRepository = imageJpaRepository;
    }

    @Override
    public ImgDeleteDto imageServerApi(List<Image> images) throws IOException {
        UriComponents uriComponents = getUriComponents(deleteUploadUrlMulti,"fileIds="+getDeleteImageParam(images), "");

        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", secretKey);
        ResponseEntity<Map> response = rt.exchange(uriComponents.toUriString(), HttpMethod.DELETE, new HttpEntity<>(headers), Map.class);
        Map<String, Object> bodyVal = ImageUtil.findBodyVal(response.getBody(), new HashMap<>());
        log.debug("delete header = {}", response.getBody().get("header"));
        ImgDeleteDto imgDeleteDto = ImgDeleteDto.builder()
                .isSuccessful((boolean)bodyVal.get("isSuccessful"))
                .resultMessage((String)bodyVal.get("resultMessage"))
                .resultCode((int)bodyVal.get("resultCode"))
                .build();
        if(!imgDeleteDto.isSuccessful() && imgDeleteDto.getResultCode()!=21011) {
            throw new CouponException(ResultType.fail_img_server_upload, "쿠폰 이미지 삭제 실패");
        }
        return imgDeleteDto;
    }

    private StringBuilder getDeleteImageParam(List<Image> images) {
        StringBuilder builderStr = new StringBuilder();
        for (Image image : images) {
            builderStr.append(image.getImgServerId());
            if(isLastImage(image.getImgServerId(), images)){
                break;
            }
            builderStr.append(",");
        }
        return builderStr;
    }

    private boolean isLastImage( String serverId,List<Image> images) {
        return serverId.equals(images.get(images.size()-1).getImgServerId());
    }



}
