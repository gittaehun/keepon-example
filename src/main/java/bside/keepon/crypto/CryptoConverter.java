package bside.keepon.crypto;

import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class CryptoConverter implements AttributeConverter<String, String> {
    @Autowired
    private SeedCrypto seedCrypto;

    @Override
    public String convertToDatabaseColumn(String text) {
        return seedCrypto.encrypt(text);
    }

    @Override
    public String convertToEntityAttribute(String cipherText) {
        return seedCrypto.decrypt(cipherText);
    }
}
