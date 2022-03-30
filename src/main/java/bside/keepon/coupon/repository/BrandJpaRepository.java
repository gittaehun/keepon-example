package bside.keepon.coupon.repository;

import bside.keepon.coupon.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandJpaRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByBrandNm(String BrandNm);
}
