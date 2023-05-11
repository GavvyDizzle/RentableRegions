package com.github.gavvydizzle.rentableregions.utils;

import com.github.gavvydizzle.rentableregions.configs.SoundsConfig;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class Sounds {

    public static Sounds generalFailSound;
    public static Sounds firstRentSound, rentSound;
    public static Sounds lotteryJoinSound, lotteryLeaveSound, lotteryWinSound, lotteryLoseSound;
    public static Sounds teleportClickSound;

    static {
        generalFailSound = new Sounds(Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1, 1);

        firstRentSound = new Sounds(Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1.4f);
        rentSound = new Sounds(Sound.BLOCK_NOTE_BLOCK_BIT, 1, 1.6f);

        lotteryJoinSound = new Sounds(Sound.BLOCK_NOTE_BLOCK_BIT, 1, 1.6f);
        lotteryLeaveSound = new Sounds(Sound.BLOCK_NOTE_BLOCK_BIT, 1, 0.6f);
        lotteryWinSound = new Sounds(Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
        lotteryLoseSound = new Sounds(Sound.ENTITY_VILLAGER_NO, 1, 0.85f);

        teleportClickSound = new Sounds(Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1.6f);
    }

    public static void reload() {
        FileConfiguration config = SoundsConfig.get();
        config.options().copyDefaults(true);

        addDefault(config, "generalFailSound", generalFailSound);
        addDefault(config, "firstRentSound", firstRentSound);
        addDefault(config, "rentSound", rentSound);
        addDefault(config, "lotteryJoinSound", lotteryJoinSound);
        addDefault(config, "lotteryLeaveSound", lotteryLeaveSound);
        addDefault(config, "lotteryWinSound", lotteryWinSound);
        addDefault(config, "lotteryLoseSound", lotteryLoseSound);
        addDefault(config, "teleportClickSound", teleportClickSound);

        SoundsConfig.save();

        generalFailSound = getSound(config, "generalFailSound");
        firstRentSound = getSound(config, "firstRentSound");
        rentSound = getSound(config, "rentSound");
        lotteryJoinSound = getSound(config, "lotteryJoinSound");
        lotteryLeaveSound = getSound(config, "lotteryLeaveSound");
        lotteryWinSound = getSound(config, "lotteryWinSound");
        lotteryLoseSound = getSound(config, "lotteryLoseSound");
        teleportClickSound = getSound(config, "teleportClickSound");
    }

    private static void addDefault(FileConfiguration config, String root, Sounds sound) {
        config.addDefault(root + ".enabled", true);
        config.addDefault(root + ".sound", sound.sound.toString().toUpperCase());
        config.addDefault(root + ".volume", sound.volume);
        config.addDefault(root + ".pitch", sound.pitch);
    }

    private static Sounds getSound(FileConfiguration config, String root) {
        if (!config.getBoolean(root + ".enabled")) return new Sounds(false);

        try {
            return new Sounds(Sound.valueOf(Objects.requireNonNull(config.getString(root + ".sound")).toUpperCase()),
                    (float) config.getDouble(root + ".volume"),
                    (float) config.getDouble(root + ".pitch"));
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to load the sound: " + root + ". This sound will be muted until this error is fixed.");
            return new Sounds(false);
        }
    }

    private final boolean isEnabled;
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public Sounds(boolean isEnabled) {
        this.isEnabled = isEnabled;
        this.sound = Sound.UI_BUTTON_CLICK;
        this.volume = 0;
        this.pitch = 0;
    }

    public Sounds(Sound sound, float volume, float pitch) {
        this.isEnabled = true;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    /**
     * Plays the sound for only the player to hear.
     * @param p The player to play the sound for.
     */
    public void playSound(Player p) {
        if (isEnabled) p.playSound(p.getLocation(), sound, volume, pitch);
    }

    /**
     * Plays the sound for only the player to hear.
     * If the player is online, the sound will be played.
     * @param uuid The player's uuid
     */
    public void playSound(UUID uuid) {
        if (!isEnabled) return;

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) player.playSound(player.getLocation(), sound, volume, pitch);
    }
}