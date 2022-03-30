package bside.keepon.coupon.service;

import bside.keepon.coupon.dto.find.CouponsResponseDto;
import bside.keepon.coupon.dto.find.DetailCouponDto;
import bside.keepon.coupon.dto.find.ImminentCouponsDto;
import bside.keepon.coupon.dto.save.*;
import bside.keepon.coupon.entity.Brand;
import bside.keepon.coupon.entity.Coupon;
import bside.keepon.coupon.entity.Image;
import bside.keepon.coupon.etc.CouponException;
import bside.keepon.coupon.etc.ImgType;
import bside.keepon.coupon.etc.UseStatus;
import bside.keepon.coupon.repository.CouponJpaRepository;
import bside.keepon.coupon.repository.ImageJpaRepository;
import bside.keepon.crypto.SeedCrypto;
import bside.keepon.jwt.TokenProvider;
import bside.keepon.user.dto.SnsUserInfo;
import bside.keepon.user.entity.Member;
import bside.keepon.user.etc.MemberException;
import bside.keepon.user.etc.SnsType;
import bside.keepon.user.repository.MemberJpaRepository;
import bside.keepon.user.repository.MemberRepository;
import bside.keepon.user.service.MemberService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class CouponServiceTest {

    @Autowired
    private CouponService couponService;
    @Autowired
    private CouponJpaRepository couponJpaRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    TokenProvider tokenProvider;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ImageJpaRepository imageJpaRepository;
    @Autowired ImageService imageDeleteService;
    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Autowired
    SeedCrypto seedCrypto;

    private Optional<Long> couponId;
    private Member member;
    private MultipartFile multipartFile;

    //@BeforeEach
    void setup() throws IOException {
        SnsUserInfo snsUserInfo = SnsUserInfo.builder()
                .email("test@naver.com")
                .nickname("김태훈")
                .snsId("111")
                .snsType(SnsType.naver)
                .build();
        Member member = memberService.signUp(snsUserInfo);


        //memberRepository.save(member);
        this.member = member;

        String absolutePath = getFilePath();
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
        saveDto.setMember(this.member);
        saveDto.setData(dto);
        saveDto.setImages(images);
        this.couponId = Optional.of(couponService.saveCoupon(saveDto));
    }


    @Test
    void refresh() throws IOException {
        ArrayList<Long> all = new ArrayList<>();
        //all.add(2734L);
        all.add(2731L);
        all.add(2627L);
        Optional<Member> byId = memberJpaRepository.findById(2163L);
        for(Long coupon:all){

            couponService.deleteCoupon(byId.get(), CouponDeleteDto.builder().couponId(coupon).build());
        }
    }
    @Test
    void test(){
        String findCouponJwt = tokenProvider.createToken(3250L);
        System.out.println(findCouponJwt);
        //eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIzMjUwIiwiZXhwIjoxNjQ4NjczNTUzfQ.bV4PFo8kbkOTEFKrLo1F08KvMhx1Cf7rmIdpdOplZxKrZkZyhWbleTFjTwfIsvfyp1l8gwIeVRQmNgbRhaJHFA
    }

    @Disabled
    @Test
    @DisplayName("브랜드 존재여부 확인 후 id 생성")
    void validateExistsBrand(){

        Brand brand = couponService.getBrand("BBQ");
        Assertions.assertThat(brand).isNotNull();
    }

    @Disabled
    @Test
    @DisplayName("쿠폰중복확인")
    void validateDuplicateCouponTest() {
        Brand brand = couponService.getBrand("BHC");
        Coupon coupon = Coupon.builder()
                .couponNo("30111")
                .brand(brand)
                .regDtm(LocalDateTime.now())
                .couponNm("뿌링클")
                .expDate("20210101")
                .useYn(UseStatus.N)
                .member(member)
                .build();

        couponJpaRepository.save(coupon);
        assertThrows(CouponException.class, () -> couponService.validateDuplicateCoupon(coupon.getMember().getId(), coupon.getCouponNo()));
    }

    @Test
    @DisplayName("쿠폰 list 조회 (userid)")
    void findCouponsByUserId() throws IOException, CouponException {
        Member testMember = Member.builder()
                .id(member.getId())
                .build();
        List<CouponsResponseDto> coupons = couponService.findCouponsByMemberId(testMember);

    }

    @Test
    @DisplayName("쿠폰 상세 조회")
    void findDetailCoupon() throws IOException {
        Member testMember = Member.builder()
                .id(member.getId())
                .build();
        DetailCouponDto detailCoupon = couponService.findDetailCoupon(testMember, this.couponId.get());
        Assertions.assertThat(detailCoupon).isNotNull();
    }

    @Test
    @DisplayName("사용여부 수정")
    void updateUseYn() throws IOException {
        Member testMember = Member.builder()
                .id(member.getId())
                .build();
        DetailCouponDto detailCoupon1 = couponService.findDetailCoupon(testMember, this.couponId.get());
        System.out.println("detailCoupon1.getUseYn() = " + detailCoupon1.getUseYn());
        UpdateUseYnDto useYnDto = UpdateUseYnDto.builder()
                .couponId(this.couponId.get())
                .useStatus(UseStatus.Y)
                .build();
        couponService.updateUseYn(useYnDto, testMember.getId());
        DetailCouponDto detailCoupon2 = couponService.findDetailCoupon(testMember, this.couponId.get());
        System.out.println("detailCoupon2.getUseYn() = " + detailCoupon2.getUseYn());
        assertThat(detailCoupon2.getUseYn()).isEqualTo(UseStatus.Y);
        assertThat(detailCoupon1.getUseYn()).isNotEqualTo(detailCoupon2.getUseYn());
    }

    @Test
    @DisplayName("쿠폰정보 수정")
    public void updateCouponInfo() throws IOException {

        CouponUpdateDto couponDto = CouponUpdateDto.builder()
                .BrandNm(Optional.of("파리바게트"))
                .couponNm(Optional.of("testCoupon"))
                .couponId(this.couponId.get())
                .expiredDate(Optional.empty())
                .build();
        Optional<Coupon> coupon = couponJpaRepository.findById(this.couponId.get());
        String absolutePath = getFilePath();
        MultipartFile multipartFile = new MockMultipartFile("testImg1", "test_coupon.jpg","image/jpg", new FileInputStream(new File(absolutePath + "/static/img/test_coupon.jpg")));

        Long couponId = couponService.updateCoupon( couponDto ,coupon.get().getMember().getId(), multipartFile);

        Optional<Coupon> findCoupon = couponJpaRepository.findByMemberIdAndId(coupon.get().getMember().getId(),couponId);
        System.out.println("coupon.get().getBrand().getBrandNm() = " + findCoupon.get().getBrand().getBrandNm());
        System.out.println("coupon.get().getCouponNm() = " + findCoupon.get().getCouponNm());
    }

    private String getFilePath() {
        Path path = Paths.get("src", "main", "resources");
        return path.toFile().getAbsolutePath();
    }

    @Test
    public void deleteCoupon() throws IOException {
        Optional<Coupon> coupon = couponJpaRepository.findById(this.couponId.get());
        DetailCouponDto detailCoupon1 = couponService.findDetailCoupon(coupon.get().getMember(), coupon.get().getId());
        System.out.println("detailCoupon1 = " + detailCoupon1.getCouponId());
        CouponDeleteDto couponDeleteDto = CouponDeleteDto.builder()
                .couponId(coupon.get().getId())
                .build();
        System.out.println("saveId = " + coupon.get().getId());
        couponService.deleteCoupon(coupon.get().getMember(), couponDeleteDto);
        assertThrows(CouponException.class, () -> couponService.findDetailCoupon(coupon.get().getMember(), coupon.get().getId()));
    }

    @Disabled //클라이언트에서 처리함으로 disabled
    @Test
    @DisplayName("임박쿠폰 list 조회 (userid)")
    void findImminentCoupons() throws IOException {
        List<ImminentCouponsDto> coupons = couponService.findImminentCoupons(member);
        for (ImminentCouponsDto coupon : coupons) {
            System.out.println("coupon.getBrandNm() = " + coupon.getBrandNm());
        }

    }

    //@AfterEach
    void deleteImage() throws IOException {
        if(this.couponId.isPresent()){
            Optional<Coupon> coupon = couponJpaRepository.findById(this.couponId.get());
            if(coupon.isPresent()) {
                imageDeleteService.imageServerApi(coupon.get().getImages());
            }
        }
    }

}