package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.configs.CommandsConfig;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class AdminHelpCommand extends SubCommand {

    private final AdminCommandManager adminCommandManager;
    private static final int COMMANDS_PER_PAGE = 8;

    public AdminHelpCommand(AdminCommandManager adminCommandManager) {
        this.adminCommandManager = adminCommandManager;

        setName("help");
        setDescription("Opens this help menu");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " help [page]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Invalid page");
                return;
            }
        }

        ArrayList<SubCommand> subCommands = adminCommandManager.getSubcommands(); // 0-7, 8-15, 16-23 ...
        int maxPage = subCommands.size() / COMMANDS_PER_PAGE + 1;
        page = Numbers.constrain(page, 1, maxPage);

        String padding = adminCommandManager.getHelpCommandPadding().replace("{page}", String.valueOf(page)).replace("{max_page}", String.valueOf(maxPage));
        if (!padding.isEmpty()) sender.sendMessage(padding);
        for (int i = (page - 1) * 8; i < Math.min(page * 8, subCommands.size()); i++) {
            sender.sendMessage(ChatColor.GOLD + subCommands.get(i).getSyntax() + " - " + ChatColor.YELLOW + CommandsConfig.getAdminDescription(subCommands.get(i)));
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}