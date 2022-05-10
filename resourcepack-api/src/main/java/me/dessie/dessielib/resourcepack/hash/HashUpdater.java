package me.dessie.dessielib.resourcepack.hash;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Compares 2 files, and checks if they have been modified in any way.
 *
 * Uses SHA1 as a hashing algorithm.
 *
 * @see DigestUtils
 */
public class HashUpdater {

    private final File newFile;
    private final File oldFile;

    //Stores what the hash was updated to if the provided files are different.
    private String hashHex;
    private byte[] hashBytes;

    /**
     * Creates a new HashUpdater for comparing a new and old zip files.
     *
     * @param newZip The new zip file that has been generated
     * @param oldZip The old zip file to compare
     * @throws IOException If a file cannot be read
     */
    public HashUpdater(File newZip, File oldZip) throws IOException {

        this.newFile = newZip;
        this.oldFile = oldZip;

        if(!this.compare()) {
            this.hashHex = getHashAsHex(this.getNewFile());
            this.hashBytes = getHashAsBytes(this.getNewFile());
            Bukkit.getLogger().info("[ResourcePackBuilder] ResourcePack modification detected. Automatically updating the SHA-1 Hash to " + this.getHashHex());
        }
    }

    /**
     * Returns the newest file that is being compared.
     * @return The newest file
     */
    public File getNewFile() {return newFile;}

    /**
     * Returns the oldest file that is being compared.
     * @return The oldest file
     */
    public File getOldFile() {return oldFile;}

    /**
     * Returns the SHA1 hash as HEX of the new file.
     * This will return null if the files are the same.
     *
     * @return The SHA1 hash of the new file.
     */
    public String getHashHex() { return hashHex; }

    /**
     * Returns the SHA1 hash as bytes of the new file.
     * This will return null if the files are the same.
     *
     * @return The SHA1 hash of the new file.
     */
    public byte[] getHashBytes() { return hashBytes; }

    /**
     * Converts the file to a SHA1 hash as HEX.
     *
     * @param file The file to convert.
     * @return The HEX string from the SHA1 hash.
     * @throws IOException If the file cannot be read.
     */
    public static String getHashAsHex(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);

        String hex = DigestUtils.sha1Hex(stream);
        stream.close();
        return hex;
    }

    /**
     * Converts the file to a SHA1 hash as bytes.
     *
     * @param file The file to convert.
     * @return The byte array from the SHA1 hash.
     * @throws IOException If the file cannot be read.
     */
    public static byte[] getHashAsBytes(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        byte[] hex = DigestUtils.sha1(stream);
        stream.close();
        return hex;
    }

    /**
     * Compares the provided ZipFiles and determines if they are similar.
     *
     * @return True if the files are the same, false if they are different.
     * @throws IOException If IOException occurs
     */
    private boolean compare() throws IOException {
        return FileUtils.contentEquals(this.getNewFile(), this.getOldFile());
    }
}
