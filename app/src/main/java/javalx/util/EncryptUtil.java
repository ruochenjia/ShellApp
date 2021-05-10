package javalx.util;

import com.mindrex.io.KeyUtils;
import com.mindrex.io.TextReader;
import com.mindrex.io.TextWriter;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.SecureRandom;
import java.security.cert.Certificate;

import javax.crypto.Cipher;

public final class EncryptUtil {
    public static Key defaultKey;
    public static Certificate defaultCert;
    public static SecureRandom defaultRandom;

    static {
        defaultKey = KeyUtils.defaultKey;
        defaultCert = KeyUtils.defaultCertificate;
        try {
            defaultRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (Exception e) {
            defaultRandom = new SecureRandom();
        }
    }

    public static void encrypt(InputStream a, OutputStream b) throws Exception {
        final Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c.init(Cipher.ENCRYPT_MODE, defaultKey, defaultRandom);
        final TextWriter d = new TextWriter(b);
        final TextReader e = new TextReader(a);
        d.write(c.doFinal(e.readBytes()));
        d.flush();
        d.close();
        e.close();
    }

    public static void encrypt0(InputStream a, OutputStream b) throws Exception {
        final Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c.init(Cipher.ENCRYPT_MODE, defaultCert, defaultRandom);
        final TextWriter d = new TextWriter(b);
        final TextReader e = new TextReader(a);
        d.write(c.doFinal(e.readBytes()));
        d.flush();
        d.close();
        // e.close();
    }

    public static void decrypt(InputStream a, OutputStream b) throws Exception {
        final Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c.init(Cipher.DECRYPT_MODE, defaultKey, defaultRandom);
        final TextWriter d = new TextWriter(b);
        final TextReader e = new TextReader(a);
        d.write(c.doFinal(e.readBytes()));
        d.flush();
        d.close();
        e.close();
    }

    public static void decrypt0(InputStream a, OutputStream b) throws Exception {
        final Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c.init(Cipher.DECRYPT_MODE, defaultCert, defaultRandom);
        final TextWriter d = new TextWriter(b);
        final TextReader e = new TextReader(a);
        d.write(c.doFinal(e.readBytes()));
        d.flush();
        d.close();
        e.close();
    }
}