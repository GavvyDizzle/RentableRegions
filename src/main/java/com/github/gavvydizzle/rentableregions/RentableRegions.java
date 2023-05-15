package com.github.gavvydizzle.rentableregions;

import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.commands.PlayerCommandManager;
import com.github.gavvydizzle.rentableregions.gui.InventoryManager;
import com.github.gavvydizzle.rentableregions.shop.ShopLogger;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.gavvydizzle.rentableregions.utils.Sounds;
import me.gavvydizzle.playerlevels.api.PlayerLevelsAPI;
import me.maximus1027.mittenrankups.MittenAPI.RankupsAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class RentableRegions extends JavaPlugin {

    private static RentableRegions instance;
    private static Economy economy;

    private ShopManager shopManager;
    private ShopLogger shopLogger;
    private AdminCommandManager adminCommandManager;
    private PlayerCommandManager playerCommandManager;

    private PlayerLevelsAPI playerLevelsAPI;
    private RankupsAPI rankupsAPI;

    @Override
    public void onEnable() {
        if (!setupEconomy() ) {
            Bukkit.getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        instance = this;

        InventoryManager inventoryManager = new InventoryManager();
        getServer().getPluginManager().registerEvents(inventoryManager, this);

        shopManager = new ShopManager(this, inventoryManager);
        getServer().getPluginManager().registerEvents(shopManager, this);
        shopManager.reload(); // Reload here instead of the constructor to avoid null issues in the Shop constructor

        shopLogger = new ShopLogger(instance);

        playerLevelsAPI = new PlayerLevelsAPI();
        rankupsAPI = new RankupsAPI();


        try {
            playerCommandManager = new PlayerCommandManager(Objects.requireNonNull(getCommand("market")), shopManager, inventoryManager);
        } catch (NullPointerException e) {
            getLogger().severe("The player command name was changed in the plugin.yml file. Please make it \"market\" and restart the server. You can change the aliases but NOT the command name.");
            getServer().getPluginManager().disablePlugin(this);
        }
        try {
            adminCommandManager = new AdminCommandManager(Objects.requireNonNull(getCommand("rr")), shopManager, inventoryManager);
        } catch (NullPointerException e) {
            getLogger().severe("The admin command name was changed in the plugin.yml file. Please make it \"rr\" and restart the server. You can change the aliases but NOT the command name.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Messages.reloadMessages();
        Sounds.reload();
    }

    @Override
    public void onDisable() {
        if (shopManager != null) {
            shopManager.saveAllShops();
            getLogger().info("Saved all shops on server shutdown");
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }


    public static RentableRegions getInstance() {
        return instance;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public ShopLogger getShopLogger() {
        return shopLogger;
    }

    public AdminCommandManager getAdminCommandManager() {
        return adminCommandManager;
    }

    public PlayerCommandManager getPlayerCommandManager() {
        return playerCommandManager;
    }

    public PlayerLevelsAPI getPlayerLevelsAPI() {
        return playerLevelsAPI;
    }

    public RankupsAPI getRankupsAPI() {
        return rankupsAPI;
    }
}
