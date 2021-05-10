package javalx.util;

import com.mindrex.annotation.TestOnly;
import com.mindrex.io.KeyUtils;
import com.mindrex.util.Sfx;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;

@TestOnly
public class EncryptedClassLoader extends ClassLoader {
    private final InputStream data;
    private final Key key;

    private EncryptedClassLoader(InputStream i, Key key, ClassLoader parent) {
        super(parent);
        this.data = i;
        this.key = key;
    }

    @Override
    public final Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> a = findLoadedClass(name);
        if (a == null) {
            Object b = getParent();
            if (b != null) {
                try {
                    a = ((ClassLoader) b).loadClass(name);
                    return Sfx.nonnull(a);
                } catch (Exception e) { // ignore
                }
            }
            EncryptUtil.defaultKey = key == null ? KeyUtils.defaultKey : key;
            b = new ByteArrayOutputStream(32);
            try {
                EncryptUtil.decrypt(data, (OutputStream) b);
            } catch (Exception e) { // ignore
            }
            b = ((ByteArrayOutputStream) b).toByteArray();
            try {
                a = defineClass(name, (byte[]) b, 0, ((byte[]) b).length);
            } catch (ClassFormatError e) { // make a checked exception
                throw new ClassNotFoundException("Unable to load class " + name, e);
            }
        }
        return a;
    }
}