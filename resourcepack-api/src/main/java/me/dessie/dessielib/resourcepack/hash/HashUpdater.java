package me.dessie.dessielib.resourcepack.hash;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class HashUpdater {

    private final File newFile;
    private final File oldFile;

    //Stores what the hash was updated to if the provided files are different.
    private String hashHex;
    private byte[] hashBytes;

    public HashUpdater(File newZip, File oldZip) throws IOException {

        this.newFile = newZip;
        this.oldFile = oldZip;

        if(!this.compare()) {
            this.hashHex = getHashAsHex(this.getNewFile());
            this.hashBytes = getHashAsBytes(this.getNewFile());
            Bukkit.getLogger().info("[ResourcePackBuilder] ResourcePack modification detected. Automatically updating the SHA-1 Hash to " + this.getHashHex());
        }
    }

    public File getNewFile() {return newFile;}
    public File getOldFile() {return oldFile;}
    public String getHashHex() { return hashHex; }
    public byte[] getHashBytes() { return hashBytes; }

    public static String getHashAsHex(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);


        String hex = DigestUtils.sha1Hex(stream);
        stream.close();
        return hex;
    }

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
