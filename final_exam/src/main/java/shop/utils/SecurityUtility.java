package shop.utils;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.mindrot.jbcrypt.BCrypt;

public final class SecurityUtility{
		public static String encryptBcrypt(String content) {
			try {
				String data =BCrypt.hashpw(content, BCrypt.gensalt(12));
				return data;
				
			}catch(Exception e) {
				
			}return "";
		}
		public static boolean compareBcrypt( String contentEncrypted, String content) {
			try {
				return BCrypt.checkpw(content, contentEncrypted);
				
			}catch(Exception e) {
				return false;
			}
		}
		public static String encryptPBEpassword(String password) {
	        int iterations = 1000;
	        char[] chars = password.toCharArray();
	        byte[] salt = getSalt();
	        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
	        SecretKeyFactory skf;
	        try {
	            skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	            byte[] hash = skf.generateSecret(spec).getEncoded();
	            return contentCrypt(hash);
	        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
	            e.printStackTrace();
	        }
	        return "";
	    }
		private static byte[] getSalt() {
			byte[] salt = new byte[16];
			try {
				SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
				sr.nextBytes(salt);
			}catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			return salt;
 		}
		private static String contentCrypt(byte[] content) {
			BigInteger bi = new BigInteger(1,content);
			String hex = bi.toString(16);
			int paddingLength = (content.length *2)- hex.length();
			if(paddingLength >0) {
				return String.format("%0", +paddingLength + "d", 0)+ hex;			
			}else {
				return hex;
			}
		}
	} 
