package com.mindrex.standardassets;

import static com.mindrex.io.IO.bufferSize;

import android.annotation.SuppressLint;

import androidx.annotation.Nullable;

import com.mindrex.annotation.Call;
import com.mindrex.collection.Array;
import com.mindrex.debugger.Log;
import com.mindrex.io.FileUtil;
import com.topjohnwu.superuser.Shell;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// Notice: This file might have been changed from original.

@Call(File.class)
@SuppressWarnings("unused")
public class Filex implements Comparable<Filex>, Comparator<Filex> {
    private final Object target;
    private final byte type;
    private final IllegalArgumentException l = new IllegalArgumentException();
    private final UnsupportedOperationException t = new UnsupportedOperationException();
    private final Attributes attr;

    // these variables are only used when accessing an archive file
    private String basePath; // the path of the archive file (/storage/emulated/0/download/test.zip)
    private String fullPath; // basePath and innerPath (/storage/emulated/0/download/test.zip/bin/test/abc.txt)
    private String innerPath; // the path related from archive root (/bin/test/abc.txt)

    public static final char separatorChar = File.separatorChar;
    public static final String separator = File.separator;
    public static byte sortingRule = 0; // 0: sort by name; 1: sort by modified date; 2: sort by size

    public Filex(Filex f) {
        this(f.getPath());
    }

    public Filex(String s) {
        this(new File(s));
    }

    public Filex(File f) {
        this.type = 1;
        this.attr = new Attributes(f);
        this.target = attr.target;
    }

    public Filex(Attributes a) {
        this.type = 2;
        this.attr = a;
        this.target = a.target;
    }

    // (parent, child)
    public Filex(Filex a, String b) {
        this(!a.getPath().endsWith(separator) ? a.getPath() + separatorChar + b : a + b);
    }

    // (parent, child)
    public Filex(String a, String b) {
        this(!a.endsWith(separator) ? a + separatorChar + b : a + b);
    }

    // (parent, child)
    public Filex(String a, Filex b) {
        this(!a.endsWith(separator) ? a + separatorChar + b.getName() : a + b.getName());
    }

    /* sd* * *@Deprecated
    // (archiveFile, innerPath)
    public Filex(ArchiveFile a, String b) {
        this.target = a;
        this.type = 3;
    }*/

    @Override
    public final int compare(Filex a, Filex b) {
        if (isDirectory()) {
            if (b.isDirectory()) if (sortingRule == 1) {
                int i = Long.compare(a.lastModified(), b.lastModified());
                if (i == 0) i = a.getPath().compareTo(b.getPath());
                return i;
            } else if (sortingRule == 2) {
                int i = Long.compare(a.size(), b.size());
                if (i == 0) i = a.getPath().compareTo(b.getPath());
                return i;
            } else return a.getPath().compareTo(b.getPath());
            else return -1;
        } else {
            if (b.isDirectory()) return 1;
            else if (sortingRule == 1) {
                int i = Long.compare(a.lastModified(), b.lastModified());
                if (i == 0) i = a.getPath().compareTo(b.getPath());
                return i;
            } else if (sortingRule == 2) {
                int i = Long.compare(a.size(), b.size());
                if (i == 0) i = a.getPath().compareTo(b.getPath());
                return i;
            } else return a.getPath().compareTo(b.getPath());
        }
    }

    @Override
    public final int compareTo(Filex f) {
        return compare(this, f);
    }

    public final <O> O getTarget() {
        return attr.getTarget();
    }

    @Deprecated // match java.io.File
    public final String getAbsolutePath() {
        return getPath();
    }

    public final String getPath() {
        return attr.path;
    }

    public final boolean isDirectory() {
        return attr.isDirectory;
    }

    public final boolean isFile() {
        return attr.isFile;
    }

    public final boolean isSymlink() {
        return attr.isSymlink;
    }

    public final boolean isBlock() {
        return attr.isBlock;
    }

    public final boolean isSocket() {
        return attr.isSocket;
    }

    public final boolean canRead() {
        return attr.canRead;
    }

    public final boolean canWrite() {
        return attr.canWrite;
    }

    public final boolean canExecute() {
        return attr.canExecute;
    }

    public final boolean exists() {
        return type != 1 || FileUtil.exists(getPath());
    }

    public final long lastModified() {
        return attr.lastModified;
    }

    public final long size() {
        return attr.size;
    }

    public final String permissions() {
        return attr.permissions;
    }

    public final boolean requireRootAccess() {
        return attr.requireRootAccess;
    }

    @Deprecated // old api
    public final String getBasePath() {
        return getPath();
    }

    @Deprecated // old api
    public final String getFullPath() {
        return getPath();
    }

    @Deprecated // old api
    public final String getInnerPath() {
        return getPath();
    }

    private static String parseParent(String s) {
        if (s.contains(separator))
            return s.substring(0, s.lastIndexOf(separatorChar));
        return null;
    }

    private static String parseFileName(String s) {
        if (s.contains(separator))
            return s.substring(s.lastIndexOf(separatorChar) + 1);
        return s;
    }

    public static void createParentDirIfNotExists(Filex f) throws Exception {
        if (f.getParent() == null) return;
        if (f.getParent().exists()) return;
        mkdir(f.getParent());
    }

    private static void a(String s) throws Exception {
        String a = parseParent(s);
        if (a == null) a = separator; // root directory
        createParentDirIfNotExists(new Filex(a));
        if (CommandUtils.hasRootAccess) {
            final Shell.Result b = Shell.su("mkdir \"" + s + "\"").exec();
            final String e = Utils.joinStringLines(b.getErr());
            if (e.length() > 0) throw new Exception(e);
        } else throw new UnsupportedOperationException("Root access required");
    }

    private static void b(String s) throws Exception {
        String a = parseParent(s);
        if (a == null) a = separator; // root directory
        createParentDirIfNotExists(new Filex(a));
        if (CommandUtils.hasRootAccess) {
            final Shell.Result b = Shell.su("touch \"" + s + "\"").exec();
            final String e = Utils.joinStringLines(b.getErr());
            if (e.length() > 0) throw new Exception(e);
        } else throw new UnsupportedOperationException("Root access required");
    }

    private static void c(String a, String b) throws Exception {
        createParentDirIfNotExists(new Filex(b));
        if (CommandUtils.hasRootAccess) {
            final Shell.Result c = Shell.su("mv \"" + a + "\" \"" + b + "\"").exec();
            final String e = Utils.joinStringLines(c.getErr());
            if (e.length() > 0) throw new Exception(e);
        } else throw new UnsupportedOperationException("Root access required");
    }

    private static void d(String a, String b) throws Exception {
        createParentDirIfNotExists(new Filex(b));
        if (CommandUtils.hasRootAccess) {
            final Shell.Result c = Shell.su("cp \"" + a + "\" \"" + b + "\"").exec();
            final String e = Utils.joinStringLines(c.getErr());
            if (e.length() > 0) throw new Exception(e);
        } else throw new UnsupportedOperationException("Root access required");
    }

    private static void e(Filex s) throws Exception {
        String a = s.getParentPath();
        if (a == null) a = separator;
        if (CommandUtils.hasRootAccess)
            if (s.isDirectory()) {
                final Shell.Result b = Shell.su("rmdir \"" + s + "\"").exec();
                final String e = Utils.joinStringLines(b.getErr());
                if (e.length() > 0) throw new Exception(e);
            } else {
                final Shell.Result b = Shell.su("rm \"" + s + "\"").exec();
                final String e = Utils.joinStringLines(b.getErr());
                if (e.length() > 0) throw new Exception(e);
            }
        else throw new UnsupportedOperationException("Root access required");
    }

    private static void f(String a, String b) throws Exception {
        createParentDirIfNotExists(new Filex(b));
        if (CommandUtils.hasRootAccess) {
            final Shell.Result c = Shell.su("ln \"" + a + "\" \"" + b + "\"").exec();
            final String e = Utils.joinStringLines(c.getErr());
            if (e.length() > 0) throw new Exception(e);
        } else throw new UnsupportedOperationException("Root access required");
    }

    private static void g(String a, String b) throws Exception {
        createParentDirIfNotExists(new Filex(b));
        if (CommandUtils.hasRootAccess) {
            final Shell.Result c = Shell.su("ln -s \"" + a + "\" \"" + b + "\"").exec();
            final String e = Utils.joinStringLines(c.getErr());
            if (e.length() > 0) throw new Exception(e);
        } else throw new UnsupportedOperationException("Root access required");
    }

    public static void mkdir(Filex f) throws Exception {
        if (f.type == 0) ((Filex) f.target).mkdir();
        else if (f.type == 1) {
            final String a = f.getPath();
            if (FileUtil.exists(a))
                throw new IOException("File " + f.getName() + " already exists");
            else try {
                FileUtil.mkdir(a);
            } catch (Exception e) {
                if (CommandUtils.hasRootAccess) a(a);
                else throw e;
            }
        } else if (f.type == 3) ; // unf
        else if (f.type == 2) a(f.getPath());
        else throw new UnsupportedOperationException();
    }

    public static void createNewFile(Filex f) throws Exception {
        if (f.type == 0) ((Filex) f.target).createNewFile();
        else if (f.type == 1) {
            final String a = f.getPath();
            if (FileUtil.exists(a))
                throw new IOException("File " + f.getName() + " already exists");
            else try {
                FileUtil.createNewFile(a);
            } catch (Exception e) {
                if (CommandUtils.hasRootAccess) b(a);
                else throw e;
            }
        } else if (f.type == 3) ; // unf
        else if (f.type == 2) b(f.getPath());
        else throw new UnsupportedOperationException();
    }

    public static void rename(Filex f, String s) throws Exception {
        if (f.type == 0) ((Filex) f.target).moveTo(s);
        else if (f.type == 1) {
            final String a = f.getPath();
            if (FileUtil.exists(s)) throw new IOException("File " + s + " already exists");
            else try {
                FileUtil.rename(a, s);
            } catch (Exception e) {
                if (CommandUtils.hasRootAccess) c(a, s);
                else throw e;
            }
        } else if (f.type == 3) ; //unf
        else if (f.type == 2) c(f.getPath(), s);
        else throw new UnsupportedOperationException();
    }

    public static void copy(Filex f, String s) throws Exception {
        if (f.type == 0) ((Filex) f.target).copyTo(s);
        else if (f.type == 1) {
            final String a = f.getPath();
            if (FileUtil.exists(s)) throw new IOException("File " + s + " already exists");
            else try {
                FileUtil.copy(a, s);
            } catch (Exception e) {
                if (CommandUtils.hasRootAccess) d(a, s);
                else throw e;
            }
        } else if (f.type == 3) ; // unf
        else if (f.type == 2) d(f.getPath(), s);
        else throw new UnsupportedOperationException();
    }

    public static void delete(Filex f) throws Exception {
        if (f.type == 0) ((Filex) f.target).delete();
        else if (f.type == 1) {
            final String a = f.getPath();
            if (!FileUtil.exists(a))
                throw new IOException("File " + f.getName() + " does not exist");
            else try {
                FileUtil.delete(a);
            } catch (Exception e) {
                if (CommandUtils.hasRootAccess) e(f);
                else throw e;
            }
        } else if (f.type == 3) ;//unf
        else if (f.type == 2) e(f);
        else throw new UnsupportedOperationException();
    }

    public final Filex mkdir() throws Exception {
        mkdir(this);
        return this;
    }

    @Deprecated // match java.io.File
    public final Filex mkdirs() throws Exception {
        return mkdir();
    }

    @SuppressWarnings("UnusedReturnValue")
    public final Filex createNewFile() throws Exception {
        createNewFile(this);
        return this;
    }

    public final void createParentDir() throws Exception {
        createParentDirIfNotExists(this);
    }

    @Deprecated
    public final void renameTo(@Deprecated String s) throws Exception {
        String a = getParentPath();
        if (a == null) a = separator;
        s = a + s;
        if (!s.equals(getPath()))
            rename(this, s);
    }

    public final void moveTo(String s) throws Exception {
        rename(this, s);
    }

    public final void moveTo(Filex f) throws Exception {
        rename(this, f.getPath());
    }

    public final void copyTo(String s) throws Exception {
        copy(this, s);
    }

    public final void copyTo(Filex f) throws Exception {
        copy(this, f.getPath());
    }


    public final void createSoftLink(String s) throws Exception {
        if (type == 0) ((Filex) target).createSoftLink(s);
        else if (type == 1) {
            final String a = getPath();
            try {
                FileUtil.createSymlink(a, s);
            } catch (Exception e) {
                if (CommandUtils.hasRootAccess) g(a, s);
                else throw e;
            }
        } else if (type == 3) ;
        else if (type == 2) g(getPath(), s);
        else throw new UnsupportedOperationException();
    }

    public final void createHardLink(String s) throws Exception {
        if (type == 0) ((Filex) target).createSoftLink(s);
        else if (type == 1) {
            final String a = getPath();
            try {
                FileUtil.createHardLink(a, s);
            } catch (Exception e) {
                if (CommandUtils.hasRootAccess) f(a, s);
                else throw e;
            }
        } else if (type == 3) ;
        else if (type == 2) f(getPath(), s);
        else throw new UnsupportedOperationException();
    }


    public final void delete() throws Exception {
        delete(this);
    }

    @Deprecated
    private InputStream getInputStream(ZipInputStream z, String s) throws Exception {
        for (ZipEntry e; (e = z.getNextEntry()) != null; ) {
            if (e.getName().equals(s)) {
                return z;
            }
        }
        throw new IOException("Entry not found");
    }

    @Deprecated
    private InputStream getInputStream(ArchiveInputStream a, String s) throws Exception {
        for (ArchiveEntry e; (e = a.getNextEntry()) != null; ) {
            if (e.getName().equals(s)) {
                return a;
            }
        }
        throw new IOException("Entry not found");
    }

    public final InputStream getInputStream() throws Exception {
        if (type == 0) return ((Filex) target).getInputStream();
        else if (type == 1) return new FileInputStream(getPath());
        else if (type == 2) return new FileInputStream(Utils.saveTempFile(getPath()));
        else throw null;
    }

    public final OutputStream getOutputStream() throws Exception {
        if (type == 0) return ((Filex) target).getOutputStream();
        else if (type == 1) return new FileOutputStream(getPath());
        else throw null;
    }

    @Nullable
    public final Filex getParent() {
        final String a = parseParent(getPath());
        return a == null ? null : new Filex(a);
    }

    @Nullable
    public final String getParentPath() {
        return parseParent(getPath());
    }

    @Nullable
    public final <O> O getParentTarget() {
        final Filex a = getParent();
        if (a == null) return null;
        else return a.getTarget();
    }

    public final String getName() {
        return parseFileName(getPath());
    }

    public final String getExtension() {
        final String a = getName().toLowerCase(Locale.ROOT);
        if (a.contains(".")) return a.substring(a.lastIndexOf("."));
        else return a;
    }

    public final Filex removeExtension() {
        return new Filex(getPath().replace(getExtension(), ""));
    }

    public final Filex[] list() {
        if (type == 0) return ((Filex) target).list();
        else if (type == 1) if (FileUtil.isDirectoryReadable(getPath())) try {
            final File[] a = ((File) target).listFiles();
            if (a == null) return new Filex[0];
            else {
                final Filex[] b = new Filex[a.length];
                for (int i = 0; i < a.length; i++) b[i] = new Filex(a[i]);
                return b;
            }
        } catch (Exception e) {
            return new Filex[0];
        }
        else return listWithCommand();
        else if (type == 2) return listWithCommand();
        else if (type == 3) throw t;
        else throw l;
    }

    private Filex[] listWithCommand() {
        if (!CommandUtils.hasRootAccess || !isDirectory())
            return new Filex[0];
        final String a = getPath();
        Filex[] u = new Filex[0];
        Filex[] v = new Filex[0];
        try {
            final Shell.Result b = Shell.su("cd \"" + a + "\"", "ls -d ./*").exec();
            for (String s : b.getOut()) {
                final Attributes c = new Attributes((Object) null);
                s = s.substring(2);
                c.isDirectory = true;
                c.isFile = false;
                c.canRead = false;
                c.canWrite = false;
                c.canExecute = false;
                c.path = a + separatorChar + s;
                c.requireRootAccess = true;
                u = Array.append(u, new Filex(c));
            }
        } catch (Exception e) {
            return new Filex[0];
        }
        try {
            final Shell.Result b = Shell.su("cd \"" + a + "\"", "ls -p | grep -v /").exec();
            for (String s : b.getOut()) {
                final Attributes c = new Attributes((Object) null);
                c.isDirectory = false;
                c.isFile = true;
                c.canRead = false;
                c.canWrite = false;
                c.canExecute = false;
                c.path = a + separatorChar + s;
                c.requireRootAccess = true;
                v = Array.append(v, new Filex(c));
            }
        } catch (Exception e) {
            return new Filex[0];
        }
        return Array.merge(u, v);
    }

    @SuppressLint("NewApi")
    public final Path toPath() {
        return FileSystems.getDefault().getPath(getPath());
    }

    public static File[] toFiles(Collection<Filex> l) {
        return toFiles(l.toArray(new Filex[0]));
    }

    public static File[] toFiles(Filex[] l) {
        final File[] a = new File[l.length];
        for (int i = 0; i < a.length; i++) a[i] = new File(l[i].getPath());
        return a;
    }

    public static List<Filex> toFilex(Collection<File> l) {
        final ArrayList<Filex> a = new ArrayList<>();
        for (File f : l) a.add(new Filex(f));
        return a;
    }

    public static List<Filex> fromString(Collection<String> l) {
        final ArrayList<Filex> a = new ArrayList<>();
        for (String f : l) a.add(new Filex(f));
        return a;
    }

    public static OutputStream copyTo(InputStream i, Filex f) throws Exception {
        try (OutputStream d = f.getOutputStream()) {
            final byte[] e = new byte[bufferSize];
            int l;
            while ((l = i.read(e)) > 0) {
                d.write(e, 0, l);
            }
            return d;
        }
    }

    public static void mkdirIfNotExists(String s) throws Exception {
        mkdirIfNotExists(new Filex(s));
    }

    public static void mkdirIfNotExists(Filex f) throws Exception {
        if (!f.exists()) f.mkdir();
    }

    public static final class Attributes {
        private boolean isDirectory = false;
        private boolean isFile = false;
        private boolean isSymlink = false;
        private boolean isBlock = false;
        private boolean isSocket = false;
        private boolean canRead = false;
        private boolean canWrite = false;
        private boolean canExecute = false;
        private String path = "null";
        private String permissions = "null";
        private long lastModified = 0;
        private long size = 0;
        private boolean requireRootAccess = false;
        private final Object target;

        public Attributes(File f) {
            this.target = f;
            final String b = f.getAbsolutePath();
            this.path = b;
            if (f.exists()) {
                try {
                    final byte c = FileUtil.fileType(b);
                    this.isDirectory = c == FileUtil.FILE_TYPE_DIRECTORY;
                    this.isFile = c == FileUtil.FILE_TYPE_REGULAR;
                    this.isSymlink = c == FileUtil.FILE_TYPE_SYMLINK;
                    this.isBlock = c == FileUtil.FILE_TYPE_BLOCK;
                    this.isSocket = c == FileUtil.FILE_TYPE_SOCKET;
                    this.canRead = FileUtil.isDirectoryReadable(b);
                    this.canWrite = FileUtil.isDirectoryWritable(b);
                    this.canExecute = f.canExecute();
                    this.permissions = FileUtil.getPermissionString(b);
                    this.lastModified = f.lastModified();
                    this.size = f.length();
                    this.requireRootAccess = !this.canRead || !this.canWrite;
                } catch (Exception e) {
                    Log.c(getClass(), e);
                }
            }
        }

        public Attributes(Object defObj) {
            this.target = defObj;
        }

        @SuppressWarnings("unchecked")
        final <O> O getTarget() {
            return (O) target;
        }

        @Override
        public final String toString() {
            return "Attributes{" +
                    "isDirectory=" + isDirectory +
                    ", isFile=" + isFile +
                    ", isSymlink=" + isSymlink +
                    ", isBlock=" + isBlock +
                    ", isSocket=" + isSocket +
                    ", canRead=" + canRead +
                    ", canWrite=" + canWrite +
                    ", canExecute=" + canExecute +
                    ", path='" + path + '\'' +
                    ", lastModified=" + lastModified +
                    ", size=" + size +
                    ", requireRootAccess=" + requireRootAccess +
                    ", target=" + target +
                    '}';
        }
    }

    @Deprecated
    @Override
    public final String toString() {
        return getPath();
    }
}