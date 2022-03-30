package bside.keepon.coupon.service;

import bside.keepon.coupon.dto.Img.ImgDeleteDto;
import bside.keepon.coupon.entity.Coupon;
import bside.keepon.coupon.entity.Image;
import bside.keepon.coupon.etc.ImgType;
import bside.keepon.coupon.repository.CouponJpaRepository;
import bside.keepon.coupon.util.ImageUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional
class ImageServiceTest {
    @Autowired
    ImageService imageSaveService;

    @Autowired
    ImageService imageDeleteService;


    @Autowired
    CouponJpaRepository couponJpaRepository;

    @Disabled
    @Test
    void imgUploadTest() throws IOException {
        Path path = Paths.get("src", "main", "resources");
        String absolutePath = path.toFile().getAbsolutePath();
        //MultipartFile multipartFile1 = new MockMultipartFile("testImg1", "test_upload_coupon.jpeg","image/jpeg", new FileInputStream(new File(absolutePath + "/static/img/test_upload_coupon.jpeg")));
        MultipartFile multipartFile2 = new MockMultipartFile("testImg2", "test_save.png","image/png", new FileInputStream(new File(absolutePath + "/static/img/test_save.png")));
        Map<ImgType, MultipartFile> images = new HashMap<>();
        images.put(ImgType.ORIGINAL, multipartFile2);
        //images.put(ImgType.ORIGINAL, multipartFile2);
        Coupon coupon = Coupon.builder()
                .id(13L)
                .build();
        imageSaveService.imageServerApi(multipartFile2);

    }

    @Disabled
    @Test
    void imgDeleteTest() throws IOException {
        Optional<Coupon> coupon = couponJpaRepository.findById(2041L);
        ImgDeleteDto result = (ImgDeleteDto)imageDeleteService.imageServerApi(coupon.get());
    }


}