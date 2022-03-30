package bside.keepon.coupon.service;

import bside.keepon.ResultType;
import bside.keepon.coupon.dto.Img.ImgDeleteDto;
import bside.keepon.coupon.dto.Img.ImgUploadDto;
import bside.keepon.coupon.dto.Img.ImgUploadParamDto;
import bside.keepon.coupon.dto.find.CouponsDataDto;
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
import bside.keepon.coupon.repository.BrandJpaRepository;
import bside.keepon.coupon.repository.CouponJpaRepository;
import bside.keepon.coupon.repository.ImageJpaRepository;
import bside.keepon.coupon.repository.projection.CouponsData;
import bside.keepon.coupon.repository.projection.ImminentCouponsData;
import bside.keepon.coupon.util.ImageUtil;
import bside.keepon.crypto.SeedCrypto;
import bside.keepon.user.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

    private final CouponJpaRepository couponJpaRepository;

    private final ImageService imageSaveService;

    private final ImageService imageDeleteService;

    private final BrandJpaRepository brandJpaRepository;

    private final ImageJpaRepository imageJpaRepository;

    private final CouponParseService couponParseService;

    private final SeedCrypto seedCrypto;

    @Value("${save.upload.url}")
    String saveUploadUrl;

    @Value("${delete.upload.url.multi}") String deleteUploadUrlMulti;

    /**
     * 쿠폰 저장 service
     * @param couponSaveDto
     * @return Long
     */
    public Long saveCoupon(CouponSaveDto couponSaveDto) throws IOException {
        Member member = couponSaveDto.getMember();
        //쿠폰 생성
        Coupon coupon = createCoupon(couponSaveDto.getData());
        //쿠폰 중복검증
        validateDuplicateCoupon(member.getId(), coupon.getCouponNo());
        //멤버 세팅
        coupon.setMember(member);
        //브랜드 생성
        coupon.setBrand(getBrand(couponSaveDto.getData().getBrandNm()));

        //쿠폰 이미지 업로드
        coupon.setImages(uploadImage(couponSaveDto.getImages(), coupon));

        //쿠폰 DB 저장
        couponJpaRepository.save(coupon);

        return coupon.getId();
    }

    protected Brand getBrand(String inputBrandNm) {
        String brandNm = couponParseService.brandNameUnify(inputBrandNm);

        return brandJpaRepository.findByBrandNm(brandNm)
                .orElse(
                    Brand.builder()
                        .brandNm(brandNm)
                        .imgName("")
                        .build()
                );
    }

    private List<Image> uploadImage(Map<ImgType, MultipartFile> images, Coupon coupon) throws IOException {
        List<Image> resultImages = new ArrayList<>();
        for (ImgType imgType : images.keySet()) {
            ImgUploadParamDto imgUploadParamDto = ImgUploadParamDto.builder()
                    .storeFileNm(ImageUtil.createStoreImgNm(images.get(imgType).getOriginalFilename()))
                    .overwrite(false)
                    .multipartFile(images.get(imgType))
                    .build();
            //이미지 서버 업로드
            ImgUploadDto dto = (ImgUploadDto)imageSaveService.imageServerApi(imgUploadParamDto);
            dto.setImgType(imgType);
            Image saveImage = createSaveImage(dto, coupon);
//            //이미지 DB저장
//            imageJpaRepository.save(saveImage);
            resultImages.add(saveImage);
        }
        return resultImages;
    }

    private Image createSaveImage(ImgUploadDto imgUploadDto, Coupon coupon) {
        //이미지 db저장
        Image image = Image.builder()
                .imgName(imgUploadDto.getName())
                .imgType(imgUploadDto.getImgType())
                .imgSize(imgUploadDto.getImage().getSize())
                .imgServerId(imgUploadDto.getServerId())
                .coupon(coupon)
                .regDtm(LocalDateTime.now())
                .build();
        return image;
    }

    private Coupon createCoupon(SaveRequestDataDto dto) {
        return Coupon.builder()
                .couponNo(dto.getCouponNo())
                .couponNm(dto.getCouponNm())
                .expDate(dto.getExpiredDate())
                .useYn(UseStatus.N)
                .regDtm(LocalDateTime.now())
                .build();
    }

    /**
     * 쿠폰 유효성 검증(존재하는 쿠폰인지)
     * @param memberId
     * @param couponNo
     */
    public void validateDuplicateCoupon(Long memberId ,String couponNo){
        if(couponJpaRepository.findByMemberIdAndCouponNo(memberId, couponNo).isPresent())
            throw new CouponException(ResultType.duplicate_coupon, "중복 쿠폰입니다.");

    }

   /**
     * 쿠폰list 조회 (userId)
     * @return List<Coupon>
     */
    public List<CouponsResponseDto> findCouponsByMemberId (Member member) {
        List<CouponsData> coupons = couponJpaRepository.findCoupons(member.getId());
        if(coupons.isEmpty()){
            throw new CouponException(ResultType.success, "존재하는 쿠폰이 없습니다");
        }
        return getCouponsData(coupons);
    }


    private List<CouponsResponseDto> getCouponsData(List<CouponsData> coupons) {
        List<CouponsResponseDto> results = new LinkedList<>();
        Map<String, List<CouponsDataDto>> map = new LinkedHashMap<>();
        Map<String, String> brandImgUrlMap = new HashMap<>();
        for (CouponsData coupon  : coupons) {
            CouponsDataDto data = new CouponsDataDto(coupon.getCouponId()
                    //, seedCrypto.decrypt(coupon.getCouponNo()) //내가원하는 dto값으로 매핑해서 가져온 건이라 직접 복호화 ..
                    ,coupon.getCouponNo()
                    , coupon.getCouponNm()
                    , ImageUtil.getFullPath(coupon.getImgNm())
                    , coupon.getExpDate()
                    , UseStatus.valueOf(coupon.getUseYn()));
            if(map.containsKey(coupon.getBrandNm())){
                map.get(coupon.getBrandNm()).add(data);

            }else{
                List<CouponsDataDto> list = new LinkedList<>();
                list.add(data);
                map.put(coupon.getBrandNm(), list);
                if (StringUtils.isBlank(coupon.getBrandImg())) {
                    brandImgUrlMap.put(coupon.getBrandNm(), "");
                } else {
                    brandImgUrlMap.put(coupon.getBrandNm(), ImageUtil.getFullPath("brand/" + coupon.getBrandImg()));
                }
            }
        }
        for (String brandNm : map.keySet()) {
            CouponsResponseDto resDto = new CouponsResponseDto(brandNm, brandImgUrlMap.get(brandNm), map.get(brandNm));
            results.add(resDto);
        }
        return results;
    }

    /**
     * 쿠폰 상세 조회
     * @param member
     * @param couponId
     * @return
     */
    public DetailCouponDto findDetailCoupon(Member member, Long couponId){
        Optional<Coupon> optionalCoupon = couponJpaRepository.findByMemberIdAndId(member.getId(), couponId);

        if(optionalCoupon.isPresent()){
            Coupon coupon = optionalCoupon.get();
            Map<ImgType, String> images = getImages(coupon);
            return DetailCouponDto.builder()
                    .couponId(coupon.getId())
                    .couponNo(coupon.getCouponNo())
                    .couponNm(coupon.getCouponNm())
                    .brandNm(coupon.getBrand().getBrandNm())
                    .expiredDate(coupon.getExpDate())
                    .useYn(coupon.getUseYn())
                    .images(images)
                    .build();
        }else{
            throw new CouponException(ResultType.success, "존재하는 쿠폰이 없습니다");
        }

    }

    private Map<ImgType, String> getImages(Coupon coupon) {
        Map<ImgType, String> imageMap = new HashMap<>();
        for (Image image : coupon.getImages()) {
            imageMap.put(image.getImgType(), ImageUtil.getFullPath(image.getImgName()));
        }

        return imageMap;
    }

    /**
     * client에서 처리하므로 사용x
     * 유효기간 임박 쿠폰list 조회 (userId)
     * @return List<Coupon>
     */
    public List<ImminentCouponsDto> findImminentCoupons (Member member) {
        List<ImminentCouponsData> coupons = couponJpaRepository.findImminentCoupons(member.getId());
        if(coupons.isEmpty()){
            throw new CouponException(ResultType.fail, "존재하는 쿠폰이 없습니다");
        }
        List<ImminentCouponsDto> imminentCoupons = new LinkedList<>();
        listCopy(coupons, imminentCoupons);

        return imminentCoupons;
    }

    private void listCopy(List<ImminentCouponsData> coupons, List<ImminentCouponsDto> imminentCoupons) {
        for (ImminentCouponsData coupon : coupons) {
            ImminentCouponsDto dto = new ImminentCouponsDto();
            BeanUtils.copyProperties(coupon, dto, ImminentCouponsDto.class);
            imminentCoupons.add(dto);
        }
    }

    public Long updateUseYn(UpdateUseYnDto updateUseYnDto, Long memberId){
        Coupon coupon = couponJpaRepository
                .findByMemberIdAndId(memberId, updateUseYnDto.getCouponId())
                .orElseThrow(() -> new CouponException(ResultType.fail, "존재하는 쿠폰이 없습니다."));
        coupon.updateUseStatus(updateUseYnDto.getUseStatus());
        return coupon.getId();
    }

    public Long updateCoupon(CouponUpdateDto couponUpdateDto, Long memberId, MultipartFile image) throws IOException {

        Optional<Coupon> coupon = couponJpaRepository.findByMemberIdAndId(memberId, couponUpdateDto.getCouponId());
        if(coupon.isPresent()){
            //쿠폰 정보/이미지 update
            updateCouponData(couponUpdateDto, coupon.get());
            //thumbnail image update
            updateImage(coupon.get(), image);
        }else{
            throw new CouponException(ResultType.fail, "존재하는 쿠폰이 없습니다.");
        }

        return coupon.get().getId();
    }

    private void updateImage(Coupon coupon, MultipartFile image) throws IOException {
//        ImgUploadParamDto imgUploadParamDto = ImgUploadParamDto.builder()
//                .storeFileNm(coupon.getImages().stream()
//                        .filter(s->s.getImgType().equals(ImgType.THUMBNAIL))
//                        .findFirst()
//                        .get().getImgName())
//                .multipartFile(image)
//                .overwrite(true)
//                .build();
//        ImgUploadDto dto = (ImgUploadDto) imageSaveService.imageServerApi(imgUploadParamDto);
        Optional<Image> thumbnail = coupon.getImages().stream().filter(s -> s.getImgType().equals(ImgType.THUMBNAIL)).findFirst();
        if(thumbnail.isPresent()) {
            List<Image> images = new ArrayList<>();
            images.add(thumbnail.get());
            imageDeleteService.imageServerApi(images);
        }
        ImgUploadParamDto dto = ImgUploadParamDto.builder()
                        .storeFileNm(ImageUtil.createStoreImgNm(image.getOriginalFilename()))
                        .multipartFile(image)
                        .overwrite(false)
                        .build();
        ImgUploadDto imgUploadDto = (ImgUploadDto)imageSaveService.imageServerApi(dto);
        coupon.setThumbnailImage(imgUploadDto);
    }



    private void updateCouponData(CouponUpdateDto couponUpdateDto, Coupon coupon) {
        coupon.updateCoupon(couponUpdateDto);
        if(couponUpdateDto.getBrandNm().isPresent()) {
            coupon.setBrand(getBrand(couponUpdateDto.getBrandNm().get()));
        }

    }

    public void deleteCoupon(Member member,CouponDeleteDto dto) throws IOException {
        Coupon coupon = couponJpaRepository.findByMemberIdAndId(member.getId(), dto.getCouponId())
                .orElseThrow(() -> new CouponException(ResultType.fail, "존재하는 쿠폰이 없습니다."));
        deleteCouponAndImages(coupon);
    }

    public int deleteCompleteCoupons() throws IOException {

        List<Coupon> coupons = couponsTobeDeleted();

        for (Coupon coupon : coupons) {
            deleteCouponAndImages(coupon);
        }
        return coupons.size();
    }

    private void deleteCouponAndImages(Coupon coupon) throws IOException {
        ImgDeleteDto deleteDto = (ImgDeleteDto)imageDeleteService.imageServerApi(coupon.getImages());
        couponJpaRepository.delete(coupon);
    }

    private List<Coupon> couponsTobeDeleted() {
        return  couponJpaRepository.findByUseDtmBefore(LocalDateTime.now().minusDays(1L));
    }

    public void deleteWthdMbrsCoupons(Long memberId) throws IOException {
        List<Coupon> coupons = findByMemberId(memberId);
        for (Coupon coupon : coupons) {
            deleteCouponAndImages(coupon);
        }
    }

    private List<Coupon> findByMemberId(Long memberId) {
        return couponJpaRepository.findByMemberId(memberId);
    }
}