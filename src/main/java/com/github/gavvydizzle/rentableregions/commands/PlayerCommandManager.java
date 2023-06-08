package com.github.gavvydizzle.rentableregions.commands;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.commands.player.*;
import com.github.gavvydizzle.rentableregions.configs.CommandsConfig;
import com.github.gavvydizzle.rentableregions.gui.InventoryManager;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.CommandManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class PlayerCommandManager extends CommandManager {

    private String helpCommandPadding;
    private final SubCommand openMenuCommand;

    public PlayerCommandManager(PluginCommand command, ShopManager shopManager, InventoryManager inventoryManager) {
        super(command);

        registerCommand(new HomeCommand(this, shopManager));
        registerCommand(new InviteCommand(this, shopManager));
        registerCommand(new JoinCommand(this, shopManager));
        registerCommand(new KickCommand(this, shopManager));
        registerCommand(new LeaveCommand(this, shopManager));
        openMenuCommand = new OpenMenuCommand(this, shopManager, inventoryManager);
        registerCommand(openMenuCommand);
        registerCommand(new PlayerHelpCommand(this));
        registerCommand(new PromoteCommand(this, shopManager));
        registerCommand(new TogglePrivacyCommand(this, shopManager));
        registerCommand(new TradeCommand(this, shopManager));
        registerCommand(new UnclaimCommand(this, shopManager));
        registerCommand(new UnInviteCommand(this, shopManager));
        registerCommand(new VisitByIDCommand(this, shopManager));
        registerCommand(new VisitCommand(this, shopManager));
        sortCommands();

        reload();
    }

    // Call before AdminCommandManager's reload
    public void reload() {
        FileConfiguration config = CommandsConfig.get();
        config.options().copyDefaults(true);

        config.addDefault("commandDisplayName.player", getCommandDisplayName());
        config.addDefault("helpCommandPadding.player", "&6-----(({page}/{max_page}) - " + RentableRegions.getInstance().getName() + " Commands)-----");

        for (SubCommand subCommand : getSubcommands()) {
            CommandsConfig.setDescriptionDefault(subCommand);
        }

        setCommandDisplayName(config.getString("commandDisplayName.player"));
        helpCommandPadding = Colors.conv(config.getString("helpCommandPadding.player"));
    }

    public String getHelpCommandPadding() {
        return helpCommandPadding;
    }

    public boolean hasMenuOpenPermission(Player player) {
        return openMenuCommand.hasPermission(player);
    }
}