package com.mindrex.android.appsupport;

import android.annotation.SuppressLint;

import com.mindrex.annotation.Call;
import com.mindrex.data.Array;
import com.mindrex.android.debugger.Log;
import com.mindrex.io.FileSystem;
import com.mindrex.io.IOUtils;
import com.mindrex.util.FilePermission;
import com.mindrex.util.Sfx;
import com.topjohnwu.superuser.Shell;

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

    public static FileSystem fs;
    public static byte sortingRule; // 0: sort by name; 1: sort by modified date; 2: sort by size

    static {
        fs = FileSystem.nativeFs;
        sortingRule = 0;
    }

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
        this(!a.getPath().endsWith(String.valueOf(FileSystem.separator)) ? a.getPath() + FileSystem.separator + b : a + b);
    }

    // (parent, child)
    public Filex(String a, String b) {
        this(!a.endsWith(String.valueOf(FileSystem.separator)) ? a + FileSystem.separator + b : a + b);
    }

    // (parent, child)
    public Filex(String a, Filex b) {
        this(!a.endsWith(String.valueOf(FileSystem.separator)) ? a + FileSystem.separator + b.getName() : a + b.getName());
    }

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

    public final FilePermission permissions() {
        return Sfx.replaceOnNull(attr.permissions, new FilePermission());
    }

    public final boolean exists() {
        return type != 1 || fs.exists(getPath());
    }

    public final long lastModified() {
        return attr.lastModified;
    }

    public final long size() {
        return attr.size;
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

    public static void createParentDirIfNotExists(Filex f) throws Exception {
        if (f.getParent() == null) return;
        if (f.getParent().exists()) return;
        mkdir(f.getParent());
    }

    private static void a(String s) throws Exception {
        String a = FileSystem.parseParent(s);
        if (a.length() == 0) a = String.valueOf(FileSystem.separator); // root directory
        createParentDirIfNotExists(new Filex(a));
        if (CommandUtils.hasRootAccess) {
            final Shell.Result b = Shell.su("mkdir \"" + s + "\"").exec();
            final String e = Utils.joinStringLines(b.getErr());
            if (e.length() > 0) throw new Exception(e);
        } else throw new UnsupportedOperationException("Root access required");
    }

    private static void b(String s) throws Exception {
        String a = FileSystem.parseParent(s);
        if (a.length() == 0) a = String.valueOf(FileSystem.separator); // root directory
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
            if (fs.exists(a))
                throw new IOException("File " + f.getName() + " already exists");
            else try {
                fs.createDirectory(a);
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
            if (fs.exists(a))
                throw new IOException("File " + f.getName() + " already exists");
            else try {
                fs.createFile(a);
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
            if (fs.exists(s)) throw new IOException("File " + s + " already exists");
            else try {
                fs.renameFile(a, s);
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
            if (fs.exists(s)) throw new IOException("File " + s + " already exists");
            else try {
                fs.copyFile(a, s);
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
            if (!fs.exists(a))
                throw new IOException("File " + f.getName() + " does not exist");
            else try {
                fs.deleteFile(a);
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
        if (a.length() == 0) a = String.valueOf(FileSystem.separator);
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
                fs.createSymlink(a, s);
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
                fs.createHardLink(a, s);
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

    public final Filex getParent() {
        final String s = getParentPath();
        if (s.length() == 0) return null;
        else return new Filex(s);
    }

    public final String getParentPath() {
        return FileSystem.parseParent(getPath());
    }


    public final <O> O getParentTarget() {
        final Filex a = getParent();
        if (a == null) return null;
        else return a.getTarget();
    }

    public final String getName() {
        return FileSystem.parseFileName(getPath());
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
        else if (type == 1) try {
            final File[] a = ((File) target).listFiles();
            if (a == null) return listWithCommand();
            else {
                final Filex[] b = new Filex[a.length];
                for (int i = 0; i < a.length; i++) b[i] = new Filex(a[i]);
                return b;
            }
        } catch (Exception e) {
            return new Filex[0];
        }
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
                c.path = a + FileSystem.separator + s;
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
                c.path = a + FileSystem.separator + s;
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
        try (OutputStream b = f.getOutputStream()) {
            IOUtils.transferStream(i, b);
            return b;
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
        private String path = "null";
        private FilePermission permissions = null;
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
                    this.isDirectory = fs.isDirectory(b);
                    this.isFile = fs.isFile(b);
                    this.isSymlink = fs.isSymlink(b);
                    this.isBlock = fs.isBlockFile(b);
                    this.isSocket = fs.isSocketFile(b);
                    this.permissions = FilePermission.init(fs.getFilePermission(b));
                    this.lastModified = f.lastModified();
                    this.size = f.length();
                    this.requireRootAccess = !f.canRead() || !f.canWrite();
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
                    ", path='" + path + '\'' +
                    ", permissions=" + permissions +
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