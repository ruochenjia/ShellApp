package com.mindrex.android.appsupport;

import androidx.annotation.NonNull;

public enum ArchiveType {
    SevenZip,
    TAR,
    ZIP,
    BROTLI,
    BZIP2,
    GZIP,
    LZ4,
    PACK200,
    SNAPPY,
    XZ,
    Z,
    ZSTD;

    @NonNull
    public static String getArchiveExtension(ArchiveType type) {
        switch (type) {
            case SevenZip:
                return ".7z";
            case TAR:
                return ".tar";
            case ZIP:
                return ".zip";
            case BZIP2:
                return ".bz2";
            case GZIP:
                return ".gz";
            case LZ4:
                return ".lz4";
            case PACK200:
                return ".pack";
            case SNAPPY:
                return ".sz";
            case XZ:
                return ".xz";
            case ZSTD:
                return ".zst";
            default:
                throw new IllegalArgumentException("Illegal archive type: " + type.toString());
        }
    }

    public static String getArchiveName(ArchiveType type) {
        switch (type) {
            case SevenZip:
                return "7zip";
            case TAR:
                return "tar";
            case ZIP:
                return "zip";
            case BZIP2:
                return "bzip2";
            case GZIP:
                return "gzip";
            case LZ4:
                return "lz4";
            case PACK200:
                return "pack200";
            case SNAPPY:
                return "snappy";
            case XZ:
                return "xz";
            case ZSTD:
                return "zstd";
            default:
                throw new IllegalArgumentException("Illegal archive type: " + type.toString());
        }
    }

    public static ArchiveType[] supportedArchiveTypes() {
        return new ArchiveType[]{
                SevenZip,
                TAR,
                ZIP,
                BZIP2,
                GZIP,
                LZ4,
                PACK200,
                SNAPPY,
                XZ,
                ZSTD
        };
    }

    public static ArchiveType[] primaryArchiveTypes() {
        return new ArchiveType[]{
                SevenZip,
                TAR,
                ZIP
        };
    }

    public static ArchiveType fromFileExtension(String fileName) {
        if (fileName.contains(".")) {
            fileName = fileName.substring(fileName.lastIndexOf(".") + 1);
            switch (fileName) {
                case "7z":
                    return SevenZip;
                case "tar":
                    return TAR;
                case "zip":
                    return ZIP;
                case "br":
                    return BROTLI;
                case "bz2":
                    return BZIP2;
                case "gz":
                    return GZIP;
                case "lz4":
                    return LZ4;
                case "pack":
                    return PACK200;
                case "sz":
                    return SNAPPY;
                case "xz":
                    return XZ;
                case "z":
                    return Z;
                case "zst":
                    return ZSTD;
                default:
                    throw new IllegalArgumentException("Unknown archive format: " + fileName);
            }
        } else throw new IllegalArgumentException("Unknown archive format: " + fileName);
    }

    ArchiveType() {
    }
}