package com.github.gavvydizzle.rentableregions.commands;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.commands.player.*;
import com.github.gavvydizzle.rentableregions.configs.CommandsConfig;
import com.github.gavvydizzle.rentableregions.gui.InventoryManager;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerCommandManager implements TabExecutor {

    private final PluginCommand command;
    private final ArrayList<SubCommand> subcommands = new ArrayList<>();
    private final ArrayList<String> subcommandStrings = new ArrayList<>();
    private final String permissionPrefix;
    private String commandDisplayName, helpCommandPadding;
    private final SubCommand openMenuCommand;

    public PlayerCommandManager(PluginCommand command, ShopManager shopManager, InventoryManager inventoryManager) {
        this.command = command;
        command.setExecutor(this);
        permissionPrefix =  command.getPermission() + ".";

        loadCommandName();

        subcommands.add(new HomeCommand(this, shopManager));
        subcommands.add(new InviteCommand(this, shopManager));
        subcommands.add(new JoinCommand(this, shopManager));
        subcommands.add(new KickCommand(this, shopManager));
        subcommands.add(new LeaveCommand(this, shopManager));
        openMenuCommand = new OpenMenuCommand(this, shopManager, inventoryManager);
        subcommands.add(openMenuCommand);
        subcommands.add(new PlayerHelpCommand(this));
        subcommands.add(new PromoteCommand(this, shopManager));
        subcommands.add(new TogglePrivacyCommand(this, shopManager));
        subcommands.add(new TradeCommand(this, shopManager));
        subcommands.add(new UnclaimCommand(this, shopManager));
        subcommands.add(new UnInviteCommand(this, shopManager));
        subcommands.add(new VisitByIDCommand(this, shopManager));
        subcommands.add(new VisitCommand(this, shopManager));
        Collections.sort(subcommands);

        for (SubCommand subCommand : subcommands) {
            subcommandStrings.add(subCommand.getName());
        }

        reload();
    }

    private void loadCommandName() {
        FileConfiguration config = CommandsConfig.get();
        config.addDefault("commandDisplayName.player", command.getName());
        commandDisplayName = config.getString("commandDisplayName.player");
    }

    // Call before AdminCommandManager's reload
    public void reload() {
        FileConfiguration config = CommandsConfig.get();
        config.options().copyDefaults(true);

        config.addDefault("commandDisplayName.player", command.getName());
        config.addDefault("helpCommandPadding.player", "&6-----(({page}/{max_page}) - " + RentableRegions.getInstance().getName() + " Commands)-----");

        for (SubCommand subCommand : subcommands) {
            CommandsConfig.setDescriptionDefault(subCommand);
        }

        commandDisplayName = config.getString("commandDisplayName.player");
        helpCommandPadding = Colors.conv(config.getString("helpCommandPadding.player"));
    }

    public String getCommandDisplayName() {
        return commandDisplayName;
    }

    public String getHelpCommandPadding() {
        return helpCommandPadding;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0) {
            for (int i = 0; i < getSubcommands().size(); i++) {
                if (args[0].equalsIgnoreCase(getSubcommands().get(i).getName())) {

                    SubCommand subCommand = subcommands.get(i);

                    if (!subCommand.hasPermission(sender)) {
                        sender.sendMessage(ChatColor.RED + "Insufficient permission");
                        return true;
                    }

                    subCommand.perform(sender, args);
                    return true;
                }
            }
            sender.sendMessage(ChatColor.RED + "Invalid command");
        }
        sender.sendMessage(ChatColor.YELLOW + "Use '/" + commandDisplayName + " help' to see a list of valid commands");

        return true;
    }

    public ArrayList<SubCommand> getSubcommands(){
        return subcommands;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            ArrayList<String> subcommandsArguments = new ArrayList<>();

            StringUtil.copyPartialMatches(args[0], subcommandStrings, subcommandsArguments);

            return subcommandsArguments;
        }
        else if (args.length >= 2) {
            for (SubCommand subcommand : subcommands) {
                if (args[0].equalsIgnoreCase(subcommand.getName())) {
                    return subcommand.getSubcommandArguments(sender, args);
                }
            }
        }

        return null;
    }

    public String getPermissionPrefix() {
        return permissionPrefix;
    }

    public PluginCommand getCommand() {
        return command;
    }

    public boolean hasMenuOpenPermission(Player player) {
        return openMenuCommand.hasPermission(player);
    }
}