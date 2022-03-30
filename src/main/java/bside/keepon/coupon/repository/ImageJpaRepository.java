package bside.keepon.coupon.repository;

import bside.keepon.coupon.entity.Coupon;
import bside.keepon.coupon.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageJpaRepository extends JpaRepository<Image, Long> {

    List<Image> findByCouponId(Long couponId);

}
