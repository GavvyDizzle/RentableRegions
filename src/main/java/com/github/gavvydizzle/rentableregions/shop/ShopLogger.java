package com.github.gavvydizzle.rentableregions.shop;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import org.bukkit.Bukkit;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logs shop actions to a text file
 */
public class ShopLogger {

    private final String folderPath;
    private final DateTimeFormatter dtf, dtf2;

    public ShopLogger(RentableRegions instance) {
        folderPath = instance.getDataFolder() + "/logs/";
        dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dtf2 = DateTimeFormatter.ofPattern("HH:mm:ss");

        File directory = new File(instance.getDataFolder() + "/logs");
        directory.mkdir();
    }

    public void writeToLog(String message) {
        String newMessage = "[" + dtf2.format(LocalDateTime.now()) + "] " + message;

        try (FileWriter fw = new FileWriter(folderPath + dtf.format(LocalDateTime.now()) + ".txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(newMessage);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to log the message: " + newMessage);
            Bukkit.getLogger().severe(e.getMessage());
        }
    }

}
