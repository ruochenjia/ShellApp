package javalx.util;

import com.mindrex.collection.Array;
import com.mindrex.io.CharsetV2;
import com.mindrex.io.FileUtil;
import com.mindrex.io.IO;
import com.mindrex.io.TextReader;
import com.mindrex.io.TextWriter;
import com.mindrex.standardassets.Storage;
import com.mindrex.util.Sfx;
import com.topjohnwu.superuser.Shell;

import java.util.Collection;
import java.util.Locale;

public class AdBlockerPlus {
    private static final String hostFile;
    private static final String backupHostFile;
    private static final String destHostFile;

    private static AdBlockerPlus ins;

    static {
        hostFile = a("\n\n/sY\u0000StE\u0000m/e\tt\tC/\u0000\u0000hO\tSt\u0000S").toString();
        backupHostFile = Storage.APPLICATION_DATA + "/hosts";
        destHostFile = a("\t\t/da\u0000tA/d\u0000Ata\u0000/hO\n\ns\u0000ts\n").toString();
    }

    private static Object a(Object a) {
        final char[] b = a.toString().toCharArray();
        char[] c = new char[0];
        for (char d : b) {
            if (d != '\n' && d != '\t' && d != '\u0000')
                c = Array.append(c, d);
        }
        return new String(c).toLowerCase(Locale.ROOT);
    }

    private AdBlockerPlus() {
    }

    public static void blockAds() throws Exception {
        if (ins == null)
            ins = new AdBlockerPlus();
        ins.block();
    }

    public static void UnblockAds() throws Exception {
        if (Sfx.ass(ins != null)) {
            ins.unblock();
            ins = null;
        }
    }

    private String a(Collection<?> a) {
        char[] b = new char[0];
        for (Object c : a)
            b = Array.append(Array.merge(b, c.toString().toCharArray()), '\n');
        return new String(b);
    }

    public final void block() throws Exception {
        final String tmpHostFile = Storage.APPLICATION_DATA + "/hosts.tmp";
        Shell.Result r = Shell.su("cp \"" + hostFile + "\" \"" + tmpHostFile + "\"").exec();
        String s = a(r.getErr());
        if (s.length() > 0) throw new Exception(s);
        r = Shell.su("cp \"" + hostFile + "\" \"" + backupHostFile + "\"").exec();
        s = a(r.getErr());
        if (s.length() > 0) throw new Exception(s);
        if (Sfx.ass(FileUtil.exists(tmpHostFile))) {
            final TextReader b = new TextReader(IO.clazz.getResourceAsStream("/hosts.ads"));
            b.charset = CharsetV2.ASCII;
            final String c = "\n" + new String(b.readBytes()) + "\n";
            b.close();
            final TextWriter a = new TextWriter(tmpHostFile, true);
            a.charset = CharsetV2.ASCII;
            a.write(c);
            a.flush();
            a.close();
            r = Shell.su("mount -o rw,remount /").exec();
            r = Shell.su("cp \"" + tmpHostFile + "\" \"" + hostFile + "\"").exec();
            s = a(r.getErr());
            if (s.length() > 0) throw new Exception(s);
            r = Shell.su("cp \"" + tmpHostFile + "\" \"" + destHostFile + "\"").exec();
            s = a(r.getErr());
            if (s.length() > 0) throw new Exception(s);
            Shell.su("mount -o ro,remount /").exec();
        }
        FileUtil.delete(tmpHostFile);
        Sfx.ass(FileUtil.exists(backupHostFile));
    }

    public final void unblock() throws Exception {
        if (FileUtil.exists(backupHostFile)) {
            Shell.su("mount -o rw,remount /").exec();
            Shell.Result a = Shell.su("cp \"" + backupHostFile + "\" \"" + hostFile + "\"").exec();
            String b = a(a.getErr());
            if (b.length() > 0) throw new Exception(b);
            a = Shell.su("cp \"" + backupHostFile + "\" \"" + destHostFile + "\"").exec();
            b = a(a.getErr());
            if (b.length() > 0) throw new Exception(b);
        }
    }
}