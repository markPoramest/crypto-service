package com.example.cryptoservice.controllers;

import com.example.cryptoservice.dto.ErrorDTO;
import com.example.cryptoservice.dto.IdCardDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

@RestController
@RequestMapping("/id-card")
public class IdCardController {

    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "secretKey";
    private static final String SALT = "salt";


    @PostMapping("/encrypt")
    public ResponseEntity<?> encrypt(@Valid @RequestBody IdCardDTO idCard) {
        try {
            String idCardNo = idCard.getIdCardNo();

            SecureRandom secureRandom = new SecureRandom();
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), ITERATION_COUNT, KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivspec);

            byte[] cipherText = cipher.doFinal(idCardNo.getBytes("UTF-8"));
            byte[] encryptedData = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encryptedData, 0, iv.length);
            System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);

            String idCardEncrypted = Base64.getEncoder().encodeToString(encryptedData);

            IdCardDTO idCardEncryptedDTO = new IdCardDTO();
            idCardEncryptedDTO.setIdCardNo(idCardEncrypted);

            return new ResponseEntity<>(idCardEncryptedDTO, HttpStatus.OK);
        } catch (Exception e) {
            ErrorDTO error = new ErrorDTO();
            error.setMessage(e.getMessage());

           return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/decrypt")
    public ResponseEntity<?> decrypt(@Valid @RequestBody IdCardDTO idCard) {
        try {
            String idCardNo = idCard.getIdCardNo();

            byte[] encryptedData = Base64.getDecoder().decode(idCardNo);
            byte[] iv = new byte[16];
            System.arraycopy(encryptedData, 0, iv, 0, iv.length);

            IvParameterSpec ivspec = new IvParameterSpec(iv);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), ITERATION_COUNT, KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);

            byte[] cipherText = new byte[encryptedData.length - 16];
            System.arraycopy(encryptedData, 16, cipherText, 0, cipherText.length);

            byte[] decryptedText = cipher.doFinal(cipherText);

            String idCardDecrypted = new String(decryptedText, "UTF-8");

            IdCardDTO idCardDecryptedDTO = new IdCardDTO();
            idCardDecryptedDTO.setIdCardNo(idCardDecrypted);
            return new ResponseEntity<>(idCardDecryptedDTO, HttpStatus.OK);
        } catch (Exception e) {
            ErrorDTO error = new ErrorDTO();
            error.setMessage(e.getMessage());

            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
