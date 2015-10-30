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
		 *  Server Setup:
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
			
			//Random number for a with 256 bits
			BigInteger a = new BigInteger(256, rand);
			
			//p is the RFC3526 4096 bit MODP group number
			String p_str = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A0864D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E208E24FA074E5AB3143DB5BFCE0FD108E4B82D120A92108011A723C12A787E6D788719A10BDBA5B2699C327186AF4E23C1A946834B6150BDA2583E9CA2AD44CE8DBBBC2DB04DE8EF92E8EFC141FBECAA6287C59474E6BC05D99B2964FA090C3A2233BA186515BE7ED1F612970CEE2D7AFB81BDD762170481CD0069127D5B05AA993B4EA988D8FDDC186FFB7DC90A6C08F4DF435C934063199FFFFFFFFFFFFFFFF";
			BigInteger p = new BigInteger(p_str, 16);
			
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
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			sha.update(k.toByteArray());
			byte[] hashed_key = sha.digest();
			byte[] key = Arrays.copyOf(hashed_key, 16);
			SecretKeySpec aes_key = new SecretKeySpec(key, "AES");
			
			//Cipher for AES is created
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			
			while(true)
			{
				//Reads input from client
				int ar_len = in.read();
				byte[] input = new byte[ar_len];
				in.read(input, 0, ar_len);
				
				//separate message and iv
				byte[] iv_part = Arrays.copyOf(input, 16);
				byte[] m = Arrays.copyOfRange(input, 16, input.length);
				
				//cipher decrypts
				IvParameterSpec iv_par = new IvParameterSpec(iv_part);
				AlgorithmParameterSpec iv_spec = iv_par;
				cipher.init(Cipher.DECRYPT_MODE, aes_key, iv_spec);
				byte[] decrypted = cipher.doFinal(m);
				
				//Message is changed
				String message = new String(decrypted, "UTF-8");
				message = "I have recieved: " + message;
				
				//cipher reencrypts with new appended iv
				byte[] iv_raw = new byte[16];
				rand.nextBytes(iv_raw);
				IvParameterSpec iv = new IvParameterSpec(iv_raw);
				AlgorithmParameterSpec spec = iv;
				cipher.init(Cipher.ENCRYPT_MODE, aes_key, spec);
				byte[] encrypted = cipher.doFinal(message.getBytes("UTF-8"));
				
				//concatenate iv and message
				byte[] output = Arrays.copyOf(iv_raw, iv_raw.length + encrypted.length);
				System.arraycopy(encrypted, 0, output, iv_raw.length, encrypted.length);
				
				//encrypted changed message is being sent to client
				out.write(output.length);
				out.write(output, 0, output.length);
			}
			
		}

	}

}
