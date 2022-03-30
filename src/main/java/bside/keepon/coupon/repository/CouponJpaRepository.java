package bside.keepon.coupon.repository;

import bside.keepon.coupon.entity.Coupon;
import bside.keepon.coupon.repository.projection.CouponsData;
import bside.keepon.coupon.repository.projection.ImminentCouponsData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Optional;

@Repository
public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    /**
     * 쿠폰 리스트 조회
     * @param memberId
     * @return
     */
    @Query(value = "SELECT X.couponId, X.couponNo, X.couponNm, X.expDate, X.useYn, X.imgNm, X.brandNm, X.brandImg fROM "+
                   "("+
                       "SELECT A.coupon_id couponId, A.coupon_no couponNo, A.coupon_nm couponNm, A.exp_date expDate, A.use_yn useYn,A.mbr_id, B.brand_id, B.brand_nm brandNm, B.img_name brandImg, C.img_id, C.img_name imgNm " +
                       "FROM coupon A inner join brand B on A.brand_id = B.brand_id inner join image C on A.coupon_id = C.coupon_id " +
                       "WHERE C.img_type = 'THUMBNAIL'  and A.mbr_id = :memberId "+
                   ") X " +
                   "INNER JOIN "+
                   "("+
                       "select coupon_id couponId,case sign(datediff(STR_TO_DATE(exp_date,'%Y%m%d'),CURDATE())) when 0 then 1 when 1 then 2 else 3 end as sign  from coupon " +
                   ") Y "+
                   "on X.couponId = Y.couponId "+
                   "group by X.couponId "+
                   "order by Y.SIGN, X.expDate",
            nativeQuery = true)
    LinkedList<CouponsData> findCoupons(@Param("memberId")Long memberId);

    /**
     * 쿠폰조회 by memberId, couponNo
     * @param memberId
     * @param couponNo
     * @return
     */
    Optional<Coupon> findByMemberIdAndCouponNo(Long memberId, String couponNo);

    /**
     * 임박쿠폰조회 (client 처리로 사용 x)
     * @param memberId
     * @return
     */
    @Query(value = "select c.brand.brandNm as brandNm, c.couponNm as couponNm from Coupon c where (datediff(STR_TO_DATE(c.expDate,'%Y%m%d'),CURDATE()) between 0 AND 7) AND c.useYn = 'N' and c.member.id = :memberId order by c.expDate")
    LinkedList<ImminentCouponsData> findImminentCoupons(@Param("memberId")Long memberId);

    /**
     * 쿠폰 조회 by memberId, couponId
     * @param memberId
     * @param couponId
     * @return
     */
    Optional<Coupon> findByMemberIdAndId(Long memberId, Long couponId);

    LinkedList<Coupon> findByMemberId(Long memberId);

    LinkedList<Coupon> findByUseDtmBefore(LocalDateTime time);
}
