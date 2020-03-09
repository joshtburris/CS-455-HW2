package cs455.scaling.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {
    
    private final String hash;
    
    public Hashing(byte[] data) {
        hash = SHA1FromBytes(data);
    }
    
    public String getHash() {
        return hash;
    }
    
    private String SHA1FromBytes(byte[] data) {
    
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
    
        while (hash.length() < Constants.HASH_BUFFER_SIZE)
            hash = "0"+ hash;
        
        assert data.length == Constants.BYTE_ARRAY_BUFFER_SIZE : "Data byte array had an invalid length";
        assert hash.length() == Constants.HASH_BUFFER_SIZE : "Hash string returned an invalid result";
        
        return hash;
    }
    
}
