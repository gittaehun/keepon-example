package bside.keepon.coupon.util;

import bside.keepon.coupon.dto.Img.ImgUploadDto;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class ImageUtil {

    private static final Logger logger = LoggerFactory.getLogger(ImageUtil.class);

    public static String imgPath;
    public static String saveUploadUrl;
    public static String deleteUploadUrl;

    @Value("${img.path}")
    public void setImgPath(String value) {
        imgPath = value;
    }


    /**
     * 랜덤으로 생성된 유니크한 파일명 생성
     * @param originalImgNm
     * @return
     */
    public static String createStoreImgNm(String originalImgNm) {
        String storeFileNm = "";
        if(!originalImgNm.isEmpty()) {
            String uuid = UUID.randomUUID().toString();
            String ext = extractExt(originalImgNm);
            storeFileNm = uuid + "." + ext;
        }
        return storeFileNm;
    }

    public static String extractExt(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    /**
     * body에 있는 값 찾아서 세팅
     * @param data
     * @param t
     */
    public static Map<String, Object> findBodyVal(Object data, Map<String, Object> resultMap){
        if(data instanceof Map){
            Map<String, Object> map = (LinkedHashMap<String, Object>) data;
            map.forEach((key, value) -> {
                //isSuccessful, id(serverId), bytes, name
                resultMap.put(key, value);
                if(map.get(key) instanceof  Map || map.get(key) instanceof List) {
                    findBodyVal(map.get(key), resultMap);
                }
            });
        }else if(data instanceof List){
            List<Map<String, Object>> list = (List<Map<String, Object>>) data;
            list.forEach((map) -> {
                findBodyVal(map, resultMap);
            });
        }

        return resultMap;
    }

    /**
     * 이미지 url 경로
     * @return
     */
    public static String getFullPath(String imgName) {
        String url = imgPath + imgName;
        return url;
    }

    public static UriComponents getUriComponents(String url,String param1, String param2) {
        return UriComponentsBuilder.fromHttpUrl(url)
                .queryParam(param1)
                .queryParam(param2)
                .build(false);

    }
}
