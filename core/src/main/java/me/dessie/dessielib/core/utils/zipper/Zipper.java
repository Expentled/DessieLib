package me.dessie.dessielib.core.utils.zipper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility for zipping a source folder into a .zip file.
 */
public class Zipper {

    /**
     * Zips a Source to a Destination
     * @param source The source folder
     * @param dest The destination for the .zip
     * @param time The FileTime to forcefully set the ZipEntry's timestamps to.
     * @throws IOException File I/O Exceptions
     */
    public Zipper(File source, File dest, FileTime time) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dest));
        walk(source, source, zos, time);
        zos.close();
    }

    /**
     * Walks the File tree to find all files to properly add.
     * This method is used to correct generate ResourcePacks, but should be viable in all .zip instances.
     *
     * @param source The source folder to zip
     * @param file   The file to zip next
     * @param stream The ZipOutputStream
     * @param time   The FileTime to forcefully set the ZipEntries timestamps to.
     *               Without this, a Zip file's SHA-1 hash will be different from a similar Zip.
     */
    private void walk(File source, File file, ZipOutputStream stream, FileTime time) {
        try {
            stream.putNextEntry(new ZipEntry((source.toPath().relativize(file.toPath()) + (file.isDirectory() ? "/" : "")).replace("\\", "/"))
                    .setLastModifiedTime(time));
            if(!file.isDirectory()) {
                Files.copy(file.toPath(), stream);
            }

            stream.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                walk(source, f, stream, time);
            }
        }
    }
}
