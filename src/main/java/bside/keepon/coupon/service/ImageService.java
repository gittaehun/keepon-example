package bside.keepon.coupon.service;

import bside.keepon.coupon.dto.Img.ImgUploadDto;

import java.io.IOException;

public interface ImageService<T, R> {

    public R imageServerApi(T t) throws IOException;

}
