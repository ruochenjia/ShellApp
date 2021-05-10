package com.mindrex.android.util;

import com.mindrex.data.Array;

import java.lang.String;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public final class DexDecompiler {
    private final Class<?>[] classes;
    private String mDcF;

    private DexDecompiler(Class<?>[] a) {
        this.classes = a;
    }

    public static DexDecompiler init(String s, ClassLoader l) throws Exception {
        String[] v = new String[0];
        try {
            final DexFile a = new DexFile(s);
            final Enumeration<String> b = a.entries();
            for (String c = ""; b.hasMoreElements(); c = b.nextElement())
                if (c.length() > 0)
                    v = Array.append(v, c);
        } catch (Exception e) {
            throw new Exception("Exception while loading DexFile", e);
        }
        final BaseDexClassLoader a = new PathClassLoader(s, l);
        final Class<?>[] b = new Class[v.length];
        for (int i = 0; i < b.length; i++)
            try {
                b[i] = a.loadClass(v[i]);
            } catch (Exception e) { // cont
            }
        return new DexDecompiler(b);
    }

    public final void decompile(String s) throws Exception {
        final Class<?> a = findClass(s);
        if (a == null)
            throw new ClassNotFoundException("Cannot find class " + s);
        else {
            final StringBuilder b = new StringBuilder()
                    .append("// Decompiled from ").append(s)
                    .append("\n");
            Object c = a.getPackage();
            if (c != null)
                b.append("\npackage ").append(((Package) c).getName()).append("\n");
            for (Annotation d : a.getDeclaredAnnotations())
                b.append("@").append(d.annotationType().getName()).append("\n");
            b.append(Modifier.toString(a.getModifiers()));
            if (a.isInterface()) b.append("interface ");
            else if (a.isAnnotation()) b.append("@interface ");
            else if (a.isEnum()) b.append("enum ");
            else b.append("class ");
            b.append(a.getSimpleName());
            if ((c = a.getSuperclass()) != null)
                b.append("extends ").append(((Class<?>) c).getName()).append(" ");
            if (((Class<?>[]) (c = a.getInterfaces())).length > 0) {
                b.append("implements ");
                for (Class<?> d : (Class<?>[]) c)
                    b.append(d.getName()).append(", ");
                b.deleteCharAt(b.length() - 2);
            }
            b.append(" {").append("\n");

            // throw new RuntimeException("stub");

            decompileFields(a, b, 1);
            b.append("\n");
        }
    }

    private Class<?> findClass(String s) {
        for (Class<?> a : classes) {
            if (a.getName().equals(s))
                return a;
        }
        return null;
    }

    private CharSequence getIndentString(int i) {
        final StringBuilder a = new StringBuilder();
        for (int b = 0; b < i; b++)
            a.append("\t");
        return a;
    }

    private void decompileFields(Class<?> a, StringBuilder b, int i) {
        final CharSequence s = getIndentString(i);
        for (Field c : a.getFields()) {
            for (Annotation d : c.getDeclaredAnnotations())
                b.append(s).append("@").append(d.annotationType().getName()).append("\n");
            b.append(s).append(Modifier.toString(c.getModifiers()))
                    .append(c.getType().getName())
                    .append(" ")
                    .append(c.getName())
                    .append(" = null;\n");
        }
    }

    private void decompileMethods(Class<?> a, StringBuilder b, int i) {
        final CharSequence s = getIndentString(i);
        for (Method c : a.getDeclaredMethods()) {
            b.append("\n");
            for (Annotation d : c.getDeclaredAnnotations()) {
                b.append("@").append(d.annotationType().getName()).append("\n");
            }
        }
    }
}