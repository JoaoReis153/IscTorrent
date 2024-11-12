package Core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
	
	public static String generateSHA256(String filePath) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
			byte[] hashBytes = digest.digest(fileBytes);

			StringBuilder hexString = new StringBuilder();
			for (byte hashByte : hashBytes) {
				String hex = Integer.toHexString(0xff & hashByte);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}

			return hexString.toString();

		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			return null; // Optionally, return an empty string or an error message instead
		}
	}

	public static void main(String[] args) {

		String file = "./dl1/doc1.txt";

		String a = generateSHA256(file);

		System.out.println(a);

	}
}
