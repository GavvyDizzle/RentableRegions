package com.github.gavvydizzle.rentableregions.commands;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.commands.admin.*;
import com.github.gavvydizzle.rentableregions.configs.CommandsConfig;
import com.github.gavvydizzle.rentableregions.gui.InventoryManager;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.CommandManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class AdminCommandManager extends CommandManager {

    private String helpCommandPadding;
    private final SubCommand signCommand;

    public AdminCommandManager(PluginCommand command, ShopManager shopManager, InventoryManager inventoryManager) {
        super(command);

        registerCommand(new AdminHelpCommand(this));
        registerCommand(new CloneCommand(this, shopManager));
        registerCommand(new CreateCommand(this, shopManager));
        registerCommand(new DeleteCommand(this, shopManager));
        registerCommand(new DumpCommand(this, shopManager));
        registerCommand(new EditMemberCommand(this, shopManager));
        registerCommand(new EditRegionCommand(this, shopManager));
        registerCommand(new EditVisitLocationCommand(this, shopManager));
        registerCommand(new InfoCommand(this, shopManager));
        registerCommand(new ReloadCommand(this, shopManager, inventoryManager));
        registerCommand(new SaveAllCommand(this, shopManager));
        registerCommand(new SetPropertyCommand(this, shopManager));
        signCommand = new SignCommand(this, shopManager);
        registerCommand(signCommand);
        registerCommand(new ToggleTimerCommand(this, shopManager));
        registerCommand(new TransferOwnershipCommand(this, shopManager));
        registerCommand(new UpdateSignsCommand(this, shopManager));
        registerCommand(new ValidateRegionsCommand(this, shopManager));
        sortCommands();

        reload();
    }

    // Call after PlayerCommandManager's reload
    public void reload() {
        FileConfiguration config = CommandsConfig.get();
        config.addDefault("commandDisplayName.admin", getCommandDisplayName());
        config.addDefault("helpCommandPadding.admin", "&6-----(({page}/{max_page}) " + RentableRegions.getInstance().getName() + " Admin Commands)-----");

        for (SubCommand subCommand : getSubcommands()) {
            CommandsConfig.setAdminDescriptionDefault(subCommand);
        }
        CommandsConfig.save();

        setCommandDisplayName(config.getString("commandDisplayName.admin"));
        helpCommandPadding = Colors.conv(config.getString("helpCommandPadding.admin"));
    }

    public String getHelpCommandPadding() {
        return helpCommandPadding;
    }

    public boolean hasSignEditingPermission(Player player) {
        return signCommand.hasPermission(player);
    }
}