import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;

public class Client {

	public static void main(String[] args) throws Exception {
		
		//Client setup
		try ( 
			    Socket server = new Socket("localhost", 8001);
				
			    OutputStream out = server.getOutputStream();
			    InputStream in = server.getInputStream();
			) {
		
			//Secure Random Number Generator is initialised
			SecureRandom rand = new SecureRandom();
			
			//All numbers are BigInts
			BigInteger g = new BigInteger("3");
		
			//a is randomly created (100 digits)
			BigInteger a = new BigInteger(100, rand);
			BigInteger p = new BigInteger("1790056198516305238528759806827943977203474413515787183411542489774464618373231629080727298");
			BigInteger A;
			
			//using modPow to calculate A
			A = g.modPow(a, p);
			
			//send A to server
			out.write(A.toByteArray().length);
			out.write(A.toByteArray(), 0, A.toByteArray().length);
			
			//Recieve B
			int length = in.read();
			byte[] array = new byte[length];
			in.read(array);
			BigInteger B = new BigInteger(array);
			
			//calculate the key
			BigInteger k = B.modPow(a, p);
			
			//Hash the key to be able to get 128 bits for AES
			String raw_key = String.valueOf(k);
			MessageDigest sha = MessageDigest.getInstance("SHA");
		    byte[] hashed_key = sha.digest(raw_key.getBytes());
			byte[] key = Arrays.copyOf(hashed_key, 16);
			SecretKeySpec aes_key = new SecretKeySpec(key, "AES");
			
			//Create cipher and dummy IV
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] iv_raw = {0x14, 0x31, 0x4c, 0x0b, 0x5a, 0x05, 0x66, 0x77, 0x0c, 0x09, 0x0a, 0x03, 0x3c, 0x1f, 0x01, 0x00};
			IvParameterSpec iv = new IvParameterSpec(iv_raw);
			
			while(true)
			{
				//ask for message
				String input = JOptionPane.showInputDialog("What message do you want to send?");
				
				//stop if there is none
				if(input == "none")
					break;
				
				//encrypt message
				AlgorithmParameterSpec spec = iv;
				cipher.init(Cipher.ENCRYPT_MODE, aes_key, spec);
				byte[] encrypted = cipher.doFinal(input.getBytes());
					
				//send it to server
				out.write(encrypted.length);
				out.write(encrypted, 0, encrypted.length);
				
				//set cipher to decrypt mode
				cipher.init(Cipher.DECRYPT_MODE, aes_key, iv);
				
				//get message
				int ar_len = -1;
				while(ar_len < 0)
				{
					ar_len = in.read();
				}
				byte[] mod_message = new byte[ar_len];
				in.read(mod_message, 0, ar_len);
			
				//decrypt
				byte[] decrypted = cipher.doFinal(mod_message);
				
				//print received message
				String final_message = new String(decrypted, "UTF-8");
				System.out.println(final_message);
			}
			
		}

	}

}
