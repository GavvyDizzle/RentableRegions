package com.github.gavvydizzle.rentableregions.commands;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.commands.admin.*;
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

public class AdminCommandManager implements TabExecutor {

    private final PluginCommand command;
    private final ArrayList<SubCommand> subcommands = new ArrayList<>();
    private final ArrayList<String> subcommandStrings = new ArrayList<>();
    private final String permissionPrefix;
    private String commandDisplayName, helpCommandPadding;
    private final SubCommand signCommand;

    public AdminCommandManager(PluginCommand command, ShopManager shopManager, InventoryManager inventoryManager) {
        this.command = command;
        command.setExecutor(this);
        permissionPrefix =  command.getPermission() + ".";

        loadCommandName();

        subcommands.add(new AdminHelpCommand(this));
        subcommands.add(new CloneCommand(this, shopManager));
        subcommands.add(new CreateCommand(this, shopManager));
        subcommands.add(new DeleteCommand(this, shopManager));
        subcommands.add(new DumpCommand(this, shopManager));
        subcommands.add(new EditMemberCommand(this, shopManager));
        subcommands.add(new EditRegionCommand(this, shopManager));
        subcommands.add(new EditVisitLocationCommand(this, shopManager));
        subcommands.add(new InfoCommand(this, shopManager));
        subcommands.add(new ReloadCommand(this, shopManager, inventoryManager));
        subcommands.add(new SaveAllCommand(this, shopManager));
        subcommands.add(new SetPropertyCommand(this, shopManager));
        signCommand = new SignCommand(this, shopManager);
        subcommands.add(signCommand);
        subcommands.add(new ToggleTimerCommand(this, shopManager));
        subcommands.add(new TransferOwnershipCommand(this, shopManager));
        subcommands.add(new UpdateSignsCommand(this, shopManager));
        Collections.sort(subcommands);

        for (SubCommand subCommand : subcommands) {
            subcommandStrings.add(subCommand.getName());
        }

        reload();
    }

    private void loadCommandName() {
        FileConfiguration config = CommandsConfig.get();
        config.addDefault("commandDisplayName.admin", command.getName());
        commandDisplayName = config.getString("commandDisplayName.admin");
    }

    // Call after PlayerCommandManager's reload
    public void reload() {
        FileConfiguration config = CommandsConfig.get();
        config.addDefault("commandDisplayName.admin", command.getName());
        config.addDefault("helpCommandPadding.admin", "&6-----(({page}/{max_page}) " + RentableRegions.getInstance().getName() + " Admin Commands)-----");

        for (SubCommand subCommand : subcommands) {
            CommandsConfig.setAdminDescriptionDefault(subCommand);
        }
        CommandsConfig.save();

        commandDisplayName = config.getString("commandDisplayName.admin");
        helpCommandPadding = Colors.conv(config.getString("helpCommandPadding.admin"));
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

    public boolean hasSignEditingPermission(Player player) {
        return signCommand.hasPermission(player);
    }
}