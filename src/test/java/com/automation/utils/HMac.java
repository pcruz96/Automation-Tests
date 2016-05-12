package com.automation.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.automation.config.TestConfiguration;

public class HMac {
	private static final String ALGORITHM = "HMACMD5";

	public String getAuthToken(String jobId) throws NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {		
		String message = TestConfiguration.getSauceLabsConfig().getString("APIKEY");
		SecretKeySpec secretKey = new SecretKeySpec(message.getBytes(), ALGORITHM);
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		mac.init(secretKey);
		byte[] result = mac.doFinal(jobId.getBytes());
		return toHexString(result).toString();
	}

	public static String toHexString(byte[] bytes) {
		StringBuffer hash = new StringBuffer();
        for (int i=0; i<bytes.length; i++) {
            String hex = Integer.toHexString(0xFF &  bytes[i]);
            if (hex.length() == 1) {
                hash.append('0');
            }
            hash.append(hex);
        }
		return hash.toString();
	}
}
