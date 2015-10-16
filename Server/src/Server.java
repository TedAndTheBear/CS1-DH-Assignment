import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Server {

	public static void main(String[] args) throws Exception {
		
		/*	
		 * 	Server Setup:
		 *  Server socket is created and accepts the Client
		 *  Output and Input Channels are connected
		 */
		
		try ( 
				ServerSocket server = new ServerSocket(8001);
				
			    Socket client = server.accept();
			    OutputStream out = client.getOutputStream();
			    InputStream in = client.getInputStream();
			) {
			
			
			//Secure Random Number Generator is used
			SecureRandom rand = new SecureRandom();
			
			//All numbers are BigInts
			BigInteger g = new BigInteger("3");
			
			//Random number for a with 100 digits
			BigInteger a = new BigInteger(100, rand);
			
			//p was randomly created
			BigInteger p = new BigInteger("1790056198516305238528759806827943977203474413515787183411542489774464618373231629080727298");
			BigInteger A;
			
			//modPow combines power of and modulo in one function
			A = g.modPow(a, p);
			
			//Send length of the byte representation of A
			out.write(A.toByteArray().length);
			
			//A is being sent so the client
			out.write(A.toByteArray(), 0, A.toByteArray().length);
			
			
			//The client's own number (B) is being read
			int length = in.read();
			byte[] array = new byte [length];
			in.read(array);
			BigInteger B = new BigInteger(array);
			
			//B -> key
			BigInteger k = B.modPow(a, p);
			
			//Key is trimmed to a specific length for the AES encryption
			//First we hash it, then we take the first 128 bit and turn it into a KeySpec
			String raw_key = String.valueOf(k);
			MessageDigest sha = MessageDigest.getInstance("SHA");
		    byte[] hashed_key = sha.digest(raw_key.getBytes());
			byte[] key = Arrays.copyOf(hashed_key, 16);
			SecretKeySpec aes_key = new SecretKeySpec(key, "AES");
			
			//Cipher for AES is created
			//The IV isn't a real IV, we know, but have no clue how to do that properly;
			//An alternative would be sending the iv, but that doesn't seem to be any safer...
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] iv_raw = {0x14, 0x31, 0x4c, 0x0b, 0x5a, 0x05, 0x66, 0x77, 0x0c, 0x09, 0x0a, 0x03, 0x3c, 0x1f, 0x01, 0x00};
			IvParameterSpec iv = new IvParameterSpec(iv_raw);
			
			while(true)
			{
				//Reads input from client
				int ar_len = in.read();
				byte[] input = new byte[ar_len];
				in.read(input, 0, ar_len);
				
				//cipher decrypts
				cipher.init(Cipher.DECRYPT_MODE, aes_key, iv);
				byte[] decrypted = cipher.doFinal(input);
				
				//Message is changed
				String message = new String(decrypted, "UTF-8");
				message = "I have recieved: " + message;
				
				//cipher reencrypts
				AlgorithmParameterSpec spec = iv;
				cipher.init(Cipher.ENCRYPT_MODE, aes_key, spec);
				byte[] encrypted = cipher.doFinal(message.getBytes());
				
				//encrypted changed message is being sent to client
				out.write(encrypted.length);
				out.write(encrypted, 0, encrypted.length);
			}
			
		}

	}

}
