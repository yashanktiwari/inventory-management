package com.inventory.util;

import java.io.File;

public class FileUtil {
    public static void copyDirectory(File source, File target) throws Exception {

        if (!target.exists()) {
            target.mkdirs();
        }

        File[] files = source.listFiles();

        if (files == null) return;

        for (File file : files) {

            File destFile = new File(target, file.getName());

            if (file.isDirectory()) {

                copyDirectory(file, destFile);

            } else {

                java.nio.file.Files.copy(
                        file.toPath(),
                        destFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            }
        }
    }

    public static void zipFolder(File sourceFolder, File zipFile) throws Exception {

        try (java.util.zip.ZipOutputStream zos =
                     new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(zipFile))) {

            zipFile(sourceFolder, sourceFolder.getName(), zos);
        }
    }

    public static void zipFile(File file, String name, java.util.zip.ZipOutputStream zos) throws Exception {

        if (file.isDirectory()) {

            for (File child : file.listFiles()) {
                zipFile(child, name + "/" + child.getName(), zos);
            }

            return;
        }

        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {

            zos.putNextEntry(new java.util.zip.ZipEntry(name));

            byte[] buffer = new byte[1024];
            int length;

            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
        }
    }

    public static void unzip(String zipFile, File destDir) throws Exception {

        byte[] buffer = new byte[1024];

        try (java.util.zip.ZipInputStream zis =
                     new java.util.zip.ZipInputStream(new java.io.FileInputStream(zipFile))) {

            java.util.zip.ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                File newFile = new File(destDir, entry.getName());

                if (entry.isDirectory()) {

                    newFile.mkdirs();

                } else {

                    newFile.getParentFile().mkdirs();

                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(newFile)) {

                        int len;

                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    public static void deleteDirectory(File dir) {

        if (!dir.exists()) return;

        for (File file : dir.listFiles()) {

            if (file.isDirectory()) deleteDirectory(file);
            else file.delete();
        }

        dir.delete();
    }

    public static void zipFolderContents(File sourceFolder, File zipFile) throws Exception {

        try (java.util.zip.ZipOutputStream zos =
                     new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(zipFile))) {

            File[] files = sourceFolder.listFiles();

            if (files == null) return;

            for (File file : files) {
                zipFileRecursive(file, file.getName(), zos);
            }
        }
    }

    private static void zipFileRecursive(File file, String entryName,
                                         java.util.zip.ZipOutputStream zos) throws Exception {

        if (file.isDirectory()) {

            if (!entryName.endsWith("/")) {
                entryName += "/";
            }

            zos.putNextEntry(new java.util.zip.ZipEntry(entryName));
            zos.closeEntry();

            File[] children = file.listFiles();

            if (children != null) {
                for (File child : children) {
                    zipFileRecursive(child, entryName + child.getName(), zos);
                }
            }

            return;
        }

        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {

            zos.putNextEntry(new java.util.zip.ZipEntry(entryName));

            byte[] buffer = new byte[4096];
            int length;

            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
        }
    }
}
