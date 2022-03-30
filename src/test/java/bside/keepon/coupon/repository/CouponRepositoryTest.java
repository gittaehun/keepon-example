package bside.keepon.coupon.repository;

import bside.keepon.coupon.dto.find.ImminentCouponsDto;
import bside.keepon.coupon.entity.Brand;
import bside.keepon.coupon.entity.Coupon;
import bside.keepon.coupon.etc.UseStatus;
import bside.keepon.coupon.repository.projection.CouponsData;
import bside.keepon.coupon.repository.projection.ImminentCouponsData;
import bside.keepon.user.entity.Member;
import bside.keepon.user.etc.SnsType;
import bside.keepon.user.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@Transactional
class CouponRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CouponJpaRepository couponJpaRepository;

    private Member member;

    @BeforeEach
    public void setup(){
        member = Member.builder()
                .nickname("thtest")
                .snsId("123")
                .snsType(SnsType.kakao)
                .signup_dtm(LocalDateTime.now())
                .build();
        memberRepository.save(member);

        Brand brand = Brand.builder()
                .brandNm("BHC")
                .build();
        Coupon coupon = Coupon.builder()
                .couponNo("111 22223334444 ")
                .member(member)
                .useYn(UseStatus.N)
                .couponNm("스타벅스 아이스아메리카노")
                .expDate("20220131")
                .regDtm(LocalDateTime.now())
                .brand(brand)
                .build();
        couponJpaRepository.save(coupon);

    }

    @Test
    public void saveCouponTest() throws Exception{

        Brand brand = Brand.builder()
                .brandNm("BHC")
                .build();
        Coupon coupon = Coupon.builder()
                .couponNo("12333 333444 ")
                .member(member)
                .useYn(UseStatus.N)
                .couponNm("스타벅스 아이스아메리카노")
                .expDate("20220131")
                .regDtm(LocalDateTime.now())
                .brand(brand)
                .build();
        couponJpaRepository.save(coupon);

        Optional<Coupon> couponData = couponJpaRepository.findById(coupon.getId());

        assertEquals("등록 쿠폰번호와 조회한 쿠폰번호는 같아야한다.",coupon.getCouponNo(), couponData.get().getCouponNo());

    }

//    @Test
//    @Disabled
//    public void findCouponsTest(){
//        System.out.println("** id = "+member.getId());
//        LinkedList<CouponsData> coupons = couponJpaRepository.findCoupons(member.getId());
//        System.out.println("coupons.size() = " + coupons.size());
//        for (CouponsData coupon : coupons) {
//            System.out.println("coupon.getExpDate() = " + coupon.getExpDate());
//        }
//
//    }

    @Test
    @Disabled
    public void findImminentCoupons(){
        List<ImminentCouponsData> imminentCoupons = couponJpaRepository.findImminentCoupons(1L);
        System.out.println("imminentCoupons.size() = " + imminentCoupons.size());
        List<ImminentCouponsDto> coupons = new LinkedList<>();
        for (ImminentCouponsData imminentCoupon : imminentCoupons) {
            ImminentCouponsDto coupon = new ImminentCouponsDto();
            BeanUtils.copyProperties(imminentCoupon, coupon);
            coupons.add(coupon);
        }

        for (ImminentCouponsDto coupon : coupons) {
            System.out.println("coupon.getBrandNm() = " + coupon.getBrandNm());
            System.out.println("coupon.getCouponNm() = " + coupon.getCouponNm());
        }

    }



}