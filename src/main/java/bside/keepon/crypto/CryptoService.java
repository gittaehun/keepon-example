package bside.keepon.crypto;

public interface CryptoService {
    public String encrypt(String couponNo);
    public String decrypt(String encryptCouponNo);
}
