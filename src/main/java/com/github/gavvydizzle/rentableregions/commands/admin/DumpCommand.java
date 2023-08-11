package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DumpCommand extends SubCommand {

    private final ShopManager shopManager;
    private final String path;
    private final DateTimeFormatter dtf;
    private final ArrayList<String> args2 = new ArrayList<>(Arrays.asList("regions", "shopids"));

    public DumpCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;
        path = RentableRegions.getInstance().getDataFolder() + "/";
        dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

        setName("dump");
        setDescription("Collect plugin information");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " dump <arg>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        String arg = args[1].toLowerCase();
        if (!args2.contains(arg)) {
            sender.sendMessage(ChatColor.RED + "Invalid argument: " + args[1]);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(RentableRegions.getInstance(), () -> {
            ArrayList<String> output = new ArrayList<>();

            // Only case, more can be added by adding branches to this if statement
            if (args[1].equalsIgnoreCase("regions")) {
                output = shopManager.getAllShopRegions();
                Collections.sort(output);
            }
            else if (args[1].equalsIgnoreCase("shopids")) {
                output = shopManager.getShopIDs();
                Collections.sort(output);
            }

            if (output.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "The dump did not happen because there was no data to write");
                return;
            }

            String fileName = dtf.format(LocalDateTime.now()) + "_" + args[1].toLowerCase() + "_dump.txt";

            try (FileWriter fw = new FileWriter(path + fileName, false);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw))
            {
                for (String str : output) {
                    out.println(str);
                }
                sender.sendMessage(ChatColor.GREEN + "Successfully created dump at plugins/RentableRegions/" + fileName);
            } catch (IOException e) {
                Bukkit.getLogger().severe("Failed to write the " + args[1].toLowerCase() + " dump");
                Bukkit.getLogger().severe(e.getMessage());
            }
        });
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], args2, list);
        }

        return list;
    }
}