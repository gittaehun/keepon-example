package bside.keepon.coupon.controller;

import bside.keepon.ResponseDto;
import bside.keepon.coupon.dto.Img.ImgDeleteDto;
import bside.keepon.coupon.dto.find.DetailCouponDto;
import bside.keepon.coupon.dto.find.ImminentCouponsDto;
import bside.keepon.coupon.dto.save.*;
import bside.keepon.coupon.etc.CouponException;
import bside.keepon.coupon.etc.ImgType;
import bside.keepon.coupon.service.CouponService;
import bside.keepon.user.entity.Member;
import bside.keepon.user.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupon")
@Slf4j
public class CouponController {

    private final CouponService couponService;
    private final MemberService memberService;

    /**
     * 쿠폰 리스트 조회
     * @return
     */
    @GetMapping("/coupons")
    public ResponseDto findCouponsByUserId(){
        try {
            return ResponseDto.getSuccessDto(couponService.findCouponsByMemberId(memberService.getCurrentUser()));
        }catch(CouponException e){
            return createExceptionResponse(ResponseDto.getSuccessDto(), e);
        }

    }

    /**
     * 쿠폰 저장
     * @param couponData
     * @param original
     * @param thumbnail
     * @return
     * @throws IOException
     */
    @PostMapping("/save")
    public ResponseDto saveCoupon(@RequestPart SaveRequestDataDto couponData,
                                  @RequestPart("original") MultipartFile original,
                                  @RequestPart("thumbnail") MultipartFile thumbnail) throws IOException {

        Map<ImgType, MultipartFile> images = createMultipartMap(original, thumbnail);
        CouponSaveDto saveDto = createSaveData(couponData, images);

        try {
            couponService.saveCoupon(saveDto);
            return ResponseDto.getSuccessDto();
        }catch (CouponException e){
            return createExceptionResponse(ResponseDto.getSuccessDto(), e);
        }
    }

    /**
     * 쿠폰 이미지 저장용 map생성
     * @param original
     * @param thumbnail
     * @return
     */
    private Map<ImgType, MultipartFile> createMultipartMap(MultipartFile original, MultipartFile thumbnail) {
        Map<ImgType, MultipartFile> images = new HashMap<>();

        images.put(ImgType.ORIGINAL, original);
        images.put(ImgType.THUMBNAIL, thumbnail);

        return images;
    }

    /**
     * 쿠폰 저장 서비스용 데이터 생성
     * @param requestData
     * @param images
     * @return
     */
    private CouponSaveDto createSaveData(SaveRequestDataDto requestData, Map<ImgType, MultipartFile> images) {
        Member currentUser = memberService.getCurrentUser();

        CouponSaveDto saveDto = CouponSaveDto.builder()
                .member(currentUser)
                .data(requestData)
                .images(images)
                .build();
        return saveDto;
    }

    /**
     * 쿠폰 상세조회
     * @param requestData
     * @return
     */
    @PostMapping("/detail-coupon")
    public ResponseDto findDetailCoupon(@RequestBody Map<String, Long> requestData){
        try{
            Member currentUser = memberService.getCurrentUser();
            DetailCouponDto coupon = couponService.findDetailCoupon(currentUser, requestData.get("couponId"));
            return ResponseDto.getSuccessDto(coupon);
        }catch(CouponException e){
            return createExceptionResponse(ResponseDto.getSuccessDto(), e);
        }

    }

    @PostMapping("/update-use")
    public ResponseDto updateUseYn(@RequestBody UpdateUseYnDto dto){
        try{
            Member currentUser = memberService.getCurrentUser();
            Long couponId = couponService.updateUseYn(dto, currentUser.getId());
            return ResponseDto.getSuccessDto();
        }catch(CouponException e){
            return createExceptionResponse(ResponseDto.getFailDto(), e);
        }
    }

    @PostMapping("/update-coupon")
    public ResponseDto updateCoupon(@RequestPart CouponUpdateDto couponData,
                                    @RequestPart("image") MultipartFile image) throws IOException {
        try{
            Member currentUser = memberService.getCurrentUser();
            couponService.updateCoupon(couponData, currentUser.getId(), image);
            return ResponseDto.getSuccessDto();
        }catch(CouponException e){
            return createExceptionResponse(ResponseDto.getFailDto(), e);
        }
    }

    @PostMapping("/delete-coupon")
    public ResponseDto deleteCoupon(@RequestBody CouponDeleteDto deleteDto) throws IOException{
        try{
            Member currentUser = memberService.getCurrentUser();
            couponService.deleteCoupon(currentUser, deleteDto);
            return ResponseDto.getSuccessDto();
        }catch(CouponException e){
            return createExceptionResponse(ResponseDto.getFailDto(), e);
        }
    }

    /**
     * 쿠폰 Exception Response 생성
     */
    private ResponseDto createExceptionResponse(ResponseDto responseDto ,CouponException e) {
        responseDto.setCode(e.getResultType().getCode());
        responseDto.setMessage(e.getMessage());
        return responseDto;
    }

    /**
     * 클라이언트 로직 추가로 사용안함
     */
    @GetMapping("/imminent-coupons")
    public ResponseDto findImminentCoupons(){

        List<ImminentCouponsDto> imminentCoupons = couponService.findImminentCoupons(memberService.getCurrentUser());

        return ResponseDto.getSuccessDto(imminentCoupons);
    }


}
