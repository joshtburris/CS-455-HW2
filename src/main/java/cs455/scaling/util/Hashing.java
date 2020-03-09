package cs455.scaling.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {
    
    public static String SHA1FromBytes(byte[] data) {
    
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("NoSuchAlgorithmException: MessageDigest was unable to get instance of algorithm SHA1");
            return "-1";
        }
    
        byte[] hashBytes = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hashBytes);
        String hash = hashInt.toString(16);
    
        while (hash.length() < 40)
            hash = "0"+ hash;
        
        assert hash.length() == 40;
        
        return hash;
    }
    
}
