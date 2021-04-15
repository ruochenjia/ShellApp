package com.mindrex.standardassets;

import static com.mindrex.io.IO.bufferSize;

import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mindrex.io.TextWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

@SuppressWarnings("unused")
public final class Utils {

    public static String formatXml(String input, int indent) throws Exception {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final Document a = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))));
            a.normalize();
            final XPath b = XPathFactory.newInstance().newXPath();
            final NodeList c = (NodeList) b.evaluate("//text()[normalize-space()='']", a, XPathConstants.NODESET);
            for (int i = 0; i < c.getLength(); ++i) {
                final Node d = c.item(i);
                d.getParentNode().removeChild(d);
            }
            final TransformerFactory e = TransformerFactory.newInstance();
            final Transformer f = e.newTransformer();
            f.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            f.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            f.setOutputProperty(OutputKeys.INDENT, "yes");
            f.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
            StringWriter g = new StringWriter();
            f.transform(new DOMSource(a), new StreamResult(g));
            return g.toString();
        } else throw new Exception("Unsupported system version");
    }

    public static String formatXml(String input) throws Exception {
        return formatXml(input, 2);
    }

    public static int dpToPixel(float f) {
        final DisplayMetrics a = Resources.getSystem().getDisplayMetrics();
        final float b = f * (a.densityDpi / 160f);
        return Math.round(b);
    }

    public static String joinStringLines(String[] lines) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return String.join("\n", lines);
        } else {
            final StringBuilder c = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                if (i == lines.length - 1) c.append(lines[i]);
                else c.append(lines[i]).append("\n");
            }
            return c.toString();
        }
    }

    public static String joinStringLines(Collection<String> lines) {
        return joinStringLines(lines.toArray(new String[0]));
    }

    public static String getDirectory(String var, String def) {
        if (var == null) return def;
        else {
            final String s = System.getenv(var);
            return s == null ? def : s;
        }
    }

    public static String getTempDir() {
        final Filex a = new Filex(Storage.APPLICATION_CACHE, ".temp");
        try {
            if (!a.isDirectory()) a.delete();
            if (!a.exists()) a.mkdir();
            return a.getPath();
        } catch (Exception e) {
            return Storage.APPLICATION_CACHE + File.separator + ".temp";
        }
    }

    public static String saveTempFile(InputStream i) throws Exception {
        final Filex a = new Filex(getTempDir(), UUID.randomUUID().toString());
        try (OutputStream d = a.getOutputStream()) {
            final byte[] e = new byte[bufferSize];
            int f;
            while ((f = i.read(e)) > 0) {
                d.write(e, 0, f);
            }
        }
        return a.getPath();
    }

    public static String saveTempFile(String path) throws Exception {
        final Filex a = new Filex(path);
        final Filex b = new Filex(getTempDir(), UUID.randomUUID().toString());
        if (b.exists()) b.delete();
        a.copyTo(b);
        return b.getPath();
    }

    static void removeTempFiles() throws Exception {
        final Filex a = new Filex(getTempDir());
        if (a.exists()) a.delete();
    }

    public static boolean toastContentDescription(View v) {
        Toast.makeText(v.getContext(), v.getContentDescription(), Toast.LENGTH_SHORT).show();
        return true;
    }

    public static final class EncryptData {
        private final String in;
        private final String out;

        public EncryptData(String in, String out) {
            this.in = in;
            this.out = out;
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        private byte[] getFile() throws Exception {
            final InputStream a = new FileInputStream(in);
            final byte[] b = new byte[a.available()];
            a.read(b);
            a.close();
            return b;
        }

        public final void encrypt(Key key) throws Exception {
            final Cipher a = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            a.init(Cipher.ENCRYPT_MODE, key, new SecureRandom());
            final TextWriter b = new TextWriter(out);
            b.write(a.doFinal(getFile()));
            b.close();
        }

        public final void decrypt(Key key) throws Exception {
            final Cipher a = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            a.init(Cipher.DECRYPT_MODE, key, new SecureRandom());
            final TextWriter b = new TextWriter(out);
            b.write(a.doFinal(getFile()));
            b.close();
        }

        public final void encrypt(Certificate cert) throws Exception {
            final Cipher a = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            a.init(Cipher.ENCRYPT_MODE, cert, new SecureRandom());
            final TextWriter b = new TextWriter(out);
            b.write(a.doFinal(getFile()));
            b.close();
        }

        public final void decrypt(Certificate cert) throws Exception {
            final Cipher a = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            a.init(Cipher.DECRYPT_MODE, cert, new SecureRandom());
            final TextWriter b = new TextWriter(out);
            b.write(a.doFinal(getFile()));
            b.close();
        }

        @NonNull
        @Override
        public final String toString() {
            return "EncryptData{" +
                    "in='" + in + '\'' +
                    ", out='" + out + '\'' +
                    '}';
        }
    }

    @Deprecated
    @SuppressWarnings("unused")
    public static final class SignJar extends com.mindrex.io.SignJar {
        public SignJar(String i, String o, PrivateKey key, X509Certificate cert) {
            super(i, o, key, cert);
        }
    }

}