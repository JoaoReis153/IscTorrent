package Core;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.Enumeration;

public class Utils {

    public static int calculateFileHash(String filePath) {
        try {
            byte[] fileContents = java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get(filePath)
            );

            byte[] hash = MessageDigest.getInstance("SHA-256").digest(
                fileContents
            );

            return new BigInteger(1, hash).intValue();
        } catch (OutOfMemoryError e) {
            System.out.println("Is too big (that's what she said) ");
            return 0;
        } catch (Exception e) {
            System.out.println("Error calculating file hash");
            return 0;
        }
    }

    public static Boolean isValidPort(int port) {
        return port > 8080 && port <= 10000;
    }

    public static Boolean isValidID(int id) {
        return id > 0 && id <= 41070;
    }

    public static InetAddress getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress instanceof Inet4Address) { // Get IPv4 address
                            return inetAddress;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null; // Return null if no address is found
    }
}
