package me.dessie.dessielib.core.utils.base64;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Utility class for converting {@link ItemStack}s or {@link Inventory}s to and from a Base64 String.
 */
public class Base64 {

    /**
     * Converts Inventory contents to a Base64 String.
     * @param inventory The Inventory to convert.
     * @return The encoded Base64 String
     * @throws IOException I/O Error
     */
    public String toBase64(Inventory inventory) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        // Write the size of the inventory
        dataOutput.writeInt(inventory.getSize());

        // Save every element in the list
        for (int i = 0; i < inventory.getSize(); i++) {
            dataOutput.writeObject(inventory.getItem(i));
        }

        // Serialize that array
        dataOutput.close();
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    /**
     * Converts an ItemStack to a Base64 String.
     * @param item The ItemStack to convert
     * @return The Base64 String
     * @throws IOException I/O Error
     */
    public String toBase64(ItemStack item) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        // Save every element in the list
        dataOutput.writeObject(item);

        // Serialize that array
        dataOutput.close();
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    /**
     * Converts a Base64 String back into an array of ItemStacks.
     * @param data The Base64 String
     * @return The decoded ItemStack array
     * @throws IOException I/O Error
     * @throws ClassNotFoundException ItemStack class not found
     */
    public ItemStack[] fromBase64(String data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());

        // Read the serialized inventory
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, (ItemStack) dataInput.readObject());
        }

        dataInput.close();
        return inventory.getContents();
    }

    /**
     * Converts a Base64 String into a single ItemStack.
     * @param data The Base64 String
     * @return The decoded ItemStack
     * @throws IOException I/O Error
     * @throws ClassNotFoundException ItemStack class not found.
     */
    public ItemStack fromBase64Item(String data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

        dataInput.close();
        return (ItemStack) dataInput.readObject();
    }
}
