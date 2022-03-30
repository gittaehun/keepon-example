package bside.keepon.coupon.controller;

import bside.keepon.coupon.dto.save.*;
import bside.keepon.coupon.entity.Brand;
import bside.keepon.coupon.entity.Coupon;
import bside.keepon.coupon.etc.ImgType;
import bside.keepon.coupon.etc.UseStatus;
import bside.keepon.coupon.repository.CouponJpaRepository;
import bside.keepon.coupon.repository.projection.CouponsData;
import bside.keepon.coupon.service.CouponService;
import bside.keepon.coupon.service.ImageService;
import bside.keepon.jwt.JwtFilter;
import bside.keepon.jwt.TokenProvider;
import bside.keepon.user.dto.SnsUserInfo;
import bside.keepon.user.entity.Member;
import bside.keepon.user.etc.SnsType;
import bside.keepon.user.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class CouponControllerTest {

    @Autowired
    TokenProvider tokenProvider;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;
    private RequestBuilder post;

    @Autowired private CouponService couponService;
    @Autowired private MemberService memberService;
    @Autowired private ImageService imageDeleteService;

    @Autowired private CouponJpaRepository couponJpaRepository;

    private Member member;

    private Optional<Long> couponId;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.couponId = Optional.empty();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new JwtFilter(tokenProvider))
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(documentationConfiguration(restDocumentation).uris()
                        .withScheme("https")
                        .withHost("keeppon.site")
                        .withPort(443))
                .build();

        SnsUserInfo snsUserInfo = SnsUserInfo.builder()
                .email("test1@naver.com")
                .nickname("김태훈")
                .snsId("222")
                .snsType(SnsType.naver)
                .build();
        Member member = memberService.signUp(snsUserInfo);

        this.member = member;
    }

    Long couponForTest() throws IOException {
        Path path = Paths.get("src", "main", "resources");
        String absolutePath = path.toFile().getAbsolutePath();
        MultipartFile multipartFile1 = new MockMultipartFile("testImg1", "test_upload_coupon.jpeg","image/jpeg", new FileInputStream(new File(absolutePath + "/static/img/test_upload_coupon.jpeg")));
        MultipartFile multipartFile2 = new MockMultipartFile("testImg2", "test_save.png","image/png", new FileInputStream(new File(absolutePath + "/static/img/test_save.png")));
        Map<ImgType, MultipartFile> images = new HashMap<>();
        images.put(ImgType.ORIGINAL, multipartFile1);
        images.put(ImgType.THUMBNAIL, multipartFile2);

        CouponSaveDto saveDto = new CouponSaveDto();
        SaveRequestDataDto dto = new SaveRequestDataDto();
        dto.setBrandNm("gs25");
        dto.setCouponNm("모바일 쿠폰 5000");
        dto.setCouponNo("1010 2222 3333 7727");
        dto.setExpiredDate("20220201");

        saveDto.setMember(member);
        saveDto.setData(dto);
        saveDto.setImages(images);
        Long couponId = couponService.saveCoupon(saveDto);
        this.couponId = Optional.of(couponId);
        return couponId;

    }

    @Test
    void 쿠폰이_없는_경우 () throws Exception {
        this.findCouponsByUserId("coupons_not_found", member.getId());
    }

    @Test
    void 쿠폰이_있는_경우 () throws Exception {
        couponForTest();
        this.findCouponsByUserId("coupons", member.getId());
    }

    private void findCouponsByUserId(String directoryName, Long memberId) throws Exception {
        String findCouponJwt = tokenProvider.createToken(memberId);

        this.mockMvc.perform(get("/coupon/coupons")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + findCouponJwt))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(directoryName,
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("code").description("00: 정상, 99: 오류"),
                                fieldWithPath("message").description("결과 메시지"),
                                subsectionWithPath("data").description("쿠폰정보 리스트 반환")
                        )
                ));
    }

    @Test
    void 쿠폰_중복_저장 () throws Exception{
        couponForTest();
        this.saveCoupon("duplicate_save", member.getId(), "1010 2222 3333 7727");
    }

    @Test
    void 쿠폰_저장_정상처리 () throws Exception {
        this.saveCoupon("success_save", member.getId(), "2022 0205 2189 1111");
    }

    private void saveCoupon(String directoryName, Long memberId, String couponNo) throws Exception {
        String saveCouponJwt = tokenProvider.createToken(memberId);
        SaveRequestDataDto dto = new SaveRequestDataDto();
        dto.setCouponNo(couponNo);
        dto.setCouponNm("모바일 상품권 5천원권");
        dto.setBrandNm("GS25");
        dto.setExpiredDate("20220201");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dto);

        System.out.println("json = " + json);

        Path path = Paths.get("src", "main", "resources");
        String absolutePath = path.toFile().getAbsolutePath();
        MockMultipartFile image1 = new MockMultipartFile("original", "test_save.png","image/png", new FileInputStream(new File(absolutePath + "/static/img/test_save.png")));
        MockMultipartFile image2 = new MockMultipartFile("thumbnail", "test_upload_coupon.jpeg","image/jpeg", new FileInputStream(new File(absolutePath + "/static/img/test_upload_coupon.jpeg")));
        MockMultipartFile jsonData = new MockMultipartFile("couponData", "", "application/json", json.getBytes());
        this.mockMvc.perform(multipart("/coupon/save")
                .file(image1)
                .file(image2)
                .file(jsonData)
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+saveCouponJwt)
                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(directoryName,
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestPartFields(
                                "couponData"
                                , fieldWithPath("couponNo").description("쿠폰 번호")
                                , fieldWithPath("couponNm").description("쿠폰명")
                                , fieldWithPath("brandNm").description("브랜드명")
                                , fieldWithPath("couponNm").description("쿠폰명")
                                , fieldWithPath("expiredDate").description("만료일")

                        ),
                        responseFields(
                                fieldWithPath("code").description("00: 정상, 99: 오류, 88 : 이미지 서버 업로드 실패, 89 : 중복 쿠폰 "),
                                fieldWithPath("message").description("결과 메시지"),
                                fieldWithPath("data").description("")
                        )
                ));
        this.couponId = Optional.of(couponJpaRepository.findByMemberIdAndCouponNo(memberId, couponNo).get().getId());
    }

    @Test
    public void 쿠폰_상세_존재() throws Exception {
        Long couponId = couponForTest();
        findDetailCouponTest("coupon_exist", member.getId(), couponId);
    }

    @Test
    public void 쿠폰_상세_미존재() throws Exception {
        findDetailCouponTest("coupon_not_exist", member.getId(), 1L);
    }


    public void findDetailCouponTest(String directoryName, Long memberId, Long couponId) throws Exception {
        String findDetailJwt = tokenProvider.createToken(memberId);
        Map<String, Long> map = new HashMap<>();
        map.put("couponId", couponId);
        String json = objectToJson(map);
        this.mockMvc.perform(post("/coupon/detail-coupon")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + findDetailJwt))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(directoryName,
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("code").description("00: 정상, 99: 오류"),
                                fieldWithPath("message").description("결과 메시지"),
                                subsectionWithPath("data").description("쿠폰상세 정보 반환")
                        )
                ));
    }

    @Test
    void 쿠폰_사용_여부_성공()throws Exception{
        Long couponId = couponForTest();
        updateUseYnTest("use_yn_success", couponId);
    }

    @Test
    void 쿠폰_사용_여부_실패()throws Exception{
        updateUseYnTest("use_yn_fail", 1L);
    }

    void updateUseYnTest(String directoryName, Long couponId) throws Exception {
        String jwt = tokenProvider.createToken(member.getId());

        UpdateUseYnDto useYnDto = UpdateUseYnDto.builder()
                .couponId(couponId)
                .useStatus(UseStatus.Y)
                .build();

        this.mockMvc.perform(post("/coupon/update-use")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectToJson(useYnDto))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(directoryName,
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("code").description("00: 정상, 99: 오류"),
                                fieldWithPath("message").description("결과 메시지"),
                                subsectionWithPath("data").description("")
                        )
                ));
    }

    @Test
    void 쿠폰_수정_성공() throws Exception {
        Long couponId = couponForTest();
        updateCouponTest("update_coupon_success", couponId);
    }

    @Test
    void 쿠폰_수정_실패() throws Exception {
        updateCouponTest("update_coupon_fail", 1L);
    }

    void updateCouponTest(String directoryName, Long couponId) throws Exception {
        String jwt = tokenProvider.createToken(member.getId());
        TestDto dto = new TestDto();
        dto.setCouponId(couponId);
        dto.setCouponNm("아몬드봉봉");
        dto.setBrandNm("배스킨라빈스");
        dto.setExpiredDate("20220221");

        String json = objectToJson(dto);
        System.out.println("json = " + json);
        Path path = Paths.get("src", "main", "resources");
        String absolutePath = path.toFile().getAbsolutePath();
        MockMultipartFile image = new MockMultipartFile("image", "test_coupon.jpg","image/jpg", new FileInputStream(new File(absolutePath + "/static/img/test_coupon.jpg")));
        MockMultipartFile jsonData = new MockMultipartFile("couponData", "", "application/json", json.getBytes());
        this.mockMvc.perform(multipart("/coupon/update-coupon")
                .file(jsonData)
                .file(image)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(directoryName,
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("code").description("00: 정상, 99: 오류, 88 : 이미지 서버 업로드 실패"),
                                fieldWithPath("message").description("결과 메시지"),
                                subsectionWithPath("data").description("")
                        )
                ));

    }

    static class TestDto{
        private Long couponId;
        private String couponNm;
        private String brandNm;
        private String expiredDate;

        public Long getCouponId() {
            return couponId;
        }

        public void setCouponId(Long couponId) {
            this.couponId = couponId;
        }

        public String getCouponNm() {
            return couponNm;
        }

        public void setCouponNm(String couponNm) {
            this.couponNm = couponNm;
        }

        public String getBrandNm() {
            return brandNm;
        }

        public void setBrandNm(String brandNm) {
            this.brandNm = brandNm;
        }

        public String getExpiredDate() {
            return expiredDate;
        }

        public void setExpiredDate(String expiredDate) {
            this.expiredDate = expiredDate;
        }
    }

    @Test
    void 쿠폰_삭제_정상() throws Exception {
        Long couponId = couponForTest();
        deleteCouponTest("delete_coupon_success", couponId);
    }
    @Test
    void 쿠폰_삭제_실패()throws Exception{
        deleteCouponTest("delete_coupon_fail",1L);
    }

    void deleteCouponTest(String directoryName, Long couponId) throws Exception {
        String jwt = tokenProvider.createToken(member.getId());
        CouponDeleteDto couponDeleteDto =CouponDeleteDto.builder().couponId(couponId).build();
        this.mockMvc.perform(post("/coupon/delete-coupon")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJson(couponDeleteDto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andDo(document(directoryName,
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("code").description("00: 정상, 99: 오류"),
                                        fieldWithPath("message").description("결과 메시지"),
                                        subsectionWithPath("data").description("")
                                )
                        ));
    }


    private String objectToJson(Object data){
        ObjectMapper mapper = new ObjectMapper();
        try{
            String json = mapper.writeValueAsString(data);
            return json;
        }catch(JsonProcessingException je){
            System.out.println("je.getMessage() = " + je.getMessage());
        }
        return "";
    }


//    @Test
//    public void findImminentCoupons(String directoryName, Long memberId) throws Exception {
//        String findImminentJwt = tokenProvider.createToken(memberId);
//        this.mockMvc.perform(get("/coupon//imminent-coupons")
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + findImminentJwt))
//                .andDo(print())
//                .andExpect(status().isOk());
////                .andDo(document(directoryName,
////                        preprocessRequest(prettyPrint()),
////                        preprocessResponse(prettyPrint()),
////                        responseFields(
////                                fieldWithPath("code").description("00: 정상, 99: 오류"),
////                                fieldWithPath("message").description("결과 메시지"),
////                                subsectionWithPath("data").description("쿠폰정보 리스트 반환")
////                        )
////                ));
//    }

    @AfterEach
    void deleteImage() throws IOException {
        if(this.couponId.isPresent()){
            Optional<Coupon> coupon = couponJpaRepository.findById(this.couponId.get());
            if(coupon.isPresent()) {
                imageDeleteService.imageServerApi(coupon.get().getImages());
            }
        }
    }
}
