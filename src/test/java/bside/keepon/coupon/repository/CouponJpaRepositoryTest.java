package bside.keepon.coupon.repository;

import bside.keepon.coupon.entity.Coupon;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.LinkedList;


@SpringBootTest
@Transactional
class CouponJpaRepositoryTest {

    @Autowired
    CouponJpaRepository couponJpaRepository;

    @Test
    void findByMemberId() {
        LinkedList<Coupon> byMemberId = couponJpaRepository.findByMemberId(1809L);
        for (Coupon coupon : byMemberId) {
            System.out.println("coupon = " + coupon.getCouponNm());
        }
    }

    @Test
    void findByUseDtmBefore() {
        LinkedList<Coupon> byUseDtmBefore = couponJpaRepository.findByUseDtmBefore(LocalDateTime.now().minusDays(1L));
        for (Coupon coupon : byUseDtmBefore) {
            System.out.println("coupon = " + coupon.getCouponNm());
        }
    }
}