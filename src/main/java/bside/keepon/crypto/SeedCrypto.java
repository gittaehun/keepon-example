package bside.keepon.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

@Component
@Slf4j
@PropertySource("classpath:crypto.properties")
public class SeedCrypto implements CryptoService {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;


    @Value("${crypto.seed.pbszuserkey}")
    private String pbszUserKey;

    @Value("${crypto.seed.pbsziv}")
    private String pbszIV;


    @Override
    public String encrypt(String text) {
        Encoder encoder = Base64.getEncoder();
        byte[] encryptedCouponNo = KISA_SEED_CBC.SEED_CBC_Encrypt(pbszUserKey.getBytes(), pbszIV.getBytes(), text.getBytes(), 0, text.getBytes().length);
        return new String(encoder.encode(encryptedCouponNo), UTF_8);
    }

    @Override
    public String decrypt(String encryptedText) {

        Decoder decoder = Base64.getDecoder();
        byte[] decodeText = decoder.decode(encryptedText);
        byte[] decryptedCouponNo = KISA_SEED_CBC.SEED_CBC_Decrypt(pbszUserKey.getBytes(), pbszIV.getBytes(), decodeText, 0, decodeText.length);
        return new String(decryptedCouponNo, UTF_8);
    }

    public String decryptForCouponNo(String couponNo){
        byte[] decodeText = Base64Utils.decodeFromUrlSafeString(couponNo);
        byte[] decryptedCouponNo = KISA_SEED_CBC.SEED_CBC_Decrypt(pbszUserKey.getBytes(), pbszIV.getBytes(), decodeText, 0, decodeText.length);
        return new String(decryptedCouponNo, UTF_8);

    }
}
