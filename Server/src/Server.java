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
			
			//Read p
			BigInteger p = new BigInteger("2");
			BigInteger fac = new BigInteger("38588805195");
			BigInteger sub = new BigInteger("1");
			p = p.pow(100002);
			p = p.multiply(fac);
			p = p.subtract(sub);
			
			BigInteger A;
			
			//modPow combines power of and modulo in one function
			A = g.modPow(a, p);
			
			//Send length of the byte representation of A
			int ALenInt = A.toByteArray().length;
			BigInteger ALen = BigInteger.valueOf(ALenInt);
			byte[] ALenB = ALen.toByteArray();
			int lenBa = ALenB.length;
			out.write(lenBa);
			out.write(ALenB, 0, lenBa);
			
			//A is being sent so the client
			out.write(A.toByteArray(), 0, A.toByteArray().length);
			
			
			//The client's own number (B) is being read
			int lengthLen = in.read();
			byte[] lenB = new byte[lengthLen];
			in.read(lenB, 0, lengthLen);
			BigInteger lenBig = new BigInteger(1, lenB);
			int length = lenBig.intValue();
			byte[] array = new byte [length];
			in.read(array);
			BigInteger B = new BigInteger(array);
			
			
			//B -> key
			BigInteger k = B.modPow(a, p);
			
			//Key is trimmed to a specific length for the AES encryption
			//First we hash it, then we take the first 128 bit and turn it into a KeySpec
			String raw_key = String.valueOf(k);
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
		    byte[] hashed_key = sha.digest(raw_key.getBytes());
			byte[] key = Arrays.copyOf(hashed_key, 16);
			SecretKeySpec aes_key = new SecretKeySpec(key, "AES");
			
			//Cipher for AES is created
			//The IV isn't ideal...
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] iv_raw = Arrays.copyOfRange(hashed_key, 17, 33);
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
