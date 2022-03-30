package bside.keepon.coupon.service;

import bside.keepon.coupon.entity.Brand;
import bside.keepon.coupon.entity.Coupon;
import bside.keepon.coupon.etc.UseStatus;
import bside.keepon.coupon.repository.BrandJpaRepository;
import bside.keepon.coupon.repository.CouponJpaRepository;
import bside.keepon.crypto.SeedCrypto;
import bside.keepon.user.entity.Member;
import bside.keepon.user.etc.SnsType;
import bside.keepon.user.repository.MemberJpaRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootTest
public class CryptoTest {
    @Autowired
    CouponJpaRepository couponJpaRepository;
    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Autowired
    BrandJpaRepository brandJpaRepository;
    @Autowired
    SeedCrypto seedCrypto;

    @Test
    public void encryptTest(){
        Member member = Member.builder()
                .email("crypto@naver.com")
                .nickname("test")
                .signup_dtm(LocalDateTime.now())
                .snsType(SnsType.kakao)
                .build();
        Member m = memberJpaRepository.save(member);


        Coupon coupon = Coupon.builder()
                    .couponNo("1111222233334444")
                    .couponNm("암호화테스트")
                    .brand(brandJpaRepository.findByBrandNm("GS25").orElse(
                          Brand.builder().brandNm("네네치킨").imgName("").build()
                    ))
                    .expDate("20220404")
                    .member(member)
                    .regDtm(LocalDateTime.now())
                    .useYn(UseStatus.N)
                    .build();
        Coupon c = couponJpaRepository.save(coupon);


        Assertions.assertThat(m.getEmail()).isEqualTo(memberJpaRepository.findById(m.getId()).get().getEmail());
        Assertions.assertThat(c.getCouponNo()).isEqualTo(couponJpaRepository.findById(c.getId()).get().getCouponNo());

    }

    @Test
    void test(){
        Optional<Coupon> coupon = couponJpaRepository.findById(2735L);
        String couponNo = coupon.get().getCouponNo();
        String encrypt = seedCrypto.encrypt(couponNo);
        System.out.println("coupon no = "+couponNo);
        System.out.println("encrypt = "+encrypt);
        System.out.println("decrypt = "+seedCrypto.decrypt(encrypt));

    }
}
