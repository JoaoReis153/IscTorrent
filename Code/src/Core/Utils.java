package Core;

import java.math.BigInteger;
import java.security.MessageDigest;

public class Utils {

    public static int calculateFileHash(byte[] fileContents) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(
                fileContents
            );

            return new BigInteger(1, hash).intValue();
        } catch (Exception e) {
            System.err.println(
                "Error calculating file hash: " + e.getMessage()
            );
            return -1;
        }
    }

    public static int calculateFileHash(String filePath) {
        try {
            byte[] fileContents = java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get(filePath)
            );
            return calculateFileHash(fileContents);
        } catch (Exception e) {
            System.err.println(
                "Error reading file for hashing: " + e.getMessage()
            );
            return -1;
        }
    }

    public static boolean verifyFileHash(
        byte[] fileContents,
        int expectedHash
    ) {
        int actualHash = calculateFileHash(fileContents);
        return actualHash == expectedHash;
    }

    public static Boolean isValidPort(int port) {
        return port > 8080 && port <= 10000;
    }

    public static Boolean isValidID(int id) {
        return id > 0 && id <= 41070;
    }
}
