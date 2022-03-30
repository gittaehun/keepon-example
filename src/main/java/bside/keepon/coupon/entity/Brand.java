package bside.keepon.coupon.entity;

import bside.keepon.coupon.dto.save.CouponUpdateDto;
import bside.keepon.coupon.etc.UseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Optional;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SequenceGenerator(
        name = "seq_brand_gen",
        sequenceName = "seq_brand",
        initialValue = 1,
        allocationSize = 1
)
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_brand_gen")
    @Column(name = "brand_id")
    private Long id;

    private String brandNm;

    private String imgName;

}
