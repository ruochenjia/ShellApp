package com.mindrex.standardassets;

import com.mindrex.io.IO;
import com.mindrex.io.FileUtil;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Deprecated
public final class ArchiveFile {
    public final String path;
    public final ArchiveType type;
    public final ArrayList<ArchiveEntry> entries;

    public ArchiveFile(String path, ArchiveType type) throws Exception {
        if (FileUtil.canAccessDirectory(path)) {
            final File f = new File(path);
            if (f.exists()) {
                this.path = path;
                this.type = type;
                this.entries = new ArrayList<>();
                if (type == ArchiveType.SevenZip) {
                    final SevenZFile a = new SevenZFile(f);
                    SevenZArchiveEntry b;
                    while ((b = a.getNextEntry()) != null) {
                        entries.add(b);
                    }
                    a.close();
                } else if (type == ArchiveType.TAR) {
                    final TarArchiveInputStream a = new TarArchiveInputStream(new FileInputStream(f));
                    TarArchiveEntry b;
                    while ((b = a.getNextTarEntry()) != null) {
                        entries.add(b);
                    }
                    a.close();
                } else if (type == ArchiveType.ZIP) {
                    final ZipArchiveInputStream a = new ZipArchiveInputStream(new FileInputStream(f));
                    ZipArchiveEntry b;
                    while ((b = a.getNextZipEntry()) != null) {
                        entries.add(b);
                    }
                    a.close();
                } else throw new UnsupportedArchiveFormatException();
            } else throw new FileNotFoundException("File " + f.getName() + " does not exist");
        } else throw new IOException("File " + path + " is not accessible");
    }

    public void addContent(InputStream s, String name) throws Exception {
        final File f = new File(path);
        final File a = File.createTempFile(UUID.randomUUID().toString(), null);
        if (!f.renameTo(a)) {
            throw new IOException("Could not create tmp file");
        }
        final byte[] buffer = new byte[IO.bufferSize];
        switch (type) {
            case ZIP:
                final ZipInputStream b = new ZipInputStream(new FileInputStream(a));
                final ZipOutputStream c = new ZipOutputStream(new FileOutputStream(f));
                c.putNextEntry(new ZipEntry(name));
                for (int i = s.read(buffer); i > -1; i = s.read(buffer)) {
                    c.write(buffer, 0, i);
                }
                c.closeEntry();
                s.close();
                for (ZipEntry e = b.getNextEntry(); e != null; e = b.getNextEntry()) {
                    c.putNextEntry(e);
                    for (int i = b.read(buffer); i > -1; i = b.read(buffer)) {
                        c.write(buffer, 0, i);
                    }
                    c.closeEntry();
                }
                c.close();
                break;
            case TAR:
                final TarArchiveInputStream g = new TarArchiveInputStream(new FileInputStream(a));
                final TarArchiveOutputStream h = new TarArchiveOutputStream(new FileOutputStream(f));
                h.putArchiveEntry(new TarArchiveEntry(name));
                for (int i = s.read(buffer); i > -1; i = s.read(buffer)) {
                    h.write(buffer, 0, i);
                }
                h.closeArchiveEntry();
                s.close();
                for (TarArchiveEntry e = g.getNextTarEntry(); e != null; e = g.getNextTarEntry()) {
                    h.putArchiveEntry(e);
                    for (int i = g.read(buffer); i > -1; i = g.read(buffer)) {
                        h.write(buffer, 0, i);
                    }
                    h.closeArchiveEntry();
                }
                h.close();
                break;
            case SevenZip:
                final SevenZFile i = new SevenZFile(a);
                final SevenZOutputFile j = new SevenZOutputFile(f);
                final SevenZArchiveEntry k = new SevenZArchiveEntry();
                k.setName(name);
                j.putArchiveEntry(k);
                for (int w = s.read(buffer); w > -1; w = s.read(buffer)) {
                    j.write(buffer, 0, w);
                }
                j.closeArchiveEntry();
                s.close();
                for (SevenZArchiveEntry e = i.getNextEntry(); e != null; e = i.getNextEntry()) {
                    j.putArchiveEntry(e);
                    for (int w = i.read(buffer); w > -1; w = i.read(buffer)) {
                        j.write(buffer, 0, w);
                    }
                    j.closeArchiveEntry();
                }
                j.close();
                break;
            default:
                throw new UnsupportedArchiveFormatException();
        }
        a.delete();
    }

    public final List<String> listContent(String rootPath) throws Exception {

        if (rootPath == null)
            rootPath = "/";
        final ArrayList<String> l = new ArrayList<>();
        for (ArchiveEntry a : entries) {
            String b = a.getName();
            if (!b.startsWith(File.separator)) b = File.separatorChar + b;
            if (b.startsWith(rootPath)) l.add(b);
        }
        Collections.sort(l);
        return l;
    }

    @Deprecated
    public final void mkdirs(String rootPath) throws Exception {
        mkdir(rootPath);
    }

    public final void mkdir(String rootPath) throws Exception {
    }

    public final void createNewFile(String rootPath) throws Exception {
    }

    public final long size(String rootPath) {
        if (rootPath == null) rootPath = "/";
        return getEntry(rootPath).getSize();
    }

    public final long lastModified(String rootPath) {
        if (rootPath == null) rootPath = "/";
        return getEntry(rootPath).getLastModifiedDate().getTime();
    }

    public final boolean isDirectory(String rootPath) {
        if (rootPath == null) rootPath = "/";
        return getEntry(rootPath).isDirectory();
    }

    public final String getFullPath(String rootPath) {
        if (rootPath == null) return path;
        else {
            String a = rootPath;
            if (a.startsWith(File.separator)) a = a.substring(2);
            String b = path;
            if (!b.endsWith(File.separator)) b = b + File.separatorChar;
            return a + b;
        }
    }

    public final ArchiveEntry getEntry(String path) {
        for (ArchiveEntry a : entries) {
            if (a.getName().equals(path)) {
                return a;
            }
        }
        return null;
    }

    public static ArchiveType verifyArchiveFile(String path) {
        try {
            final ZipInputStream a = new ZipInputStream(new FileInputStream(path));
            a.getNextEntry();
            a.close();
            return ArchiveType.ZIP;
        } catch (Exception e) {
            try {
                final SevenZFile a = new SevenZFile(new File(path));
                a.getNextEntry();
                a.close();
                return ArchiveType.SevenZip;
            } catch (Exception e1) {
                try {
                    final TarArchiveInputStream a = new TarArchiveInputStream(new FileInputStream(path));
                    a.getNextEntry();
                    a.close();
                    return ArchiveType.TAR;
                } catch (Exception e2) {
                    return null;
                }
            }
        }
    }

    private final class UnsupportedArchiveFormatException extends IllegalArgumentException {
        private static final long serialVersionUID = -22001556258325739L;

        private UnsupportedArchiveFormatException() {
            super("Unsupported archive format:" + type);
        }
    }
}