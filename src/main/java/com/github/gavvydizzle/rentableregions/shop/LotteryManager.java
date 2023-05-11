package com.github.gavvydizzle.rentableregions.shop;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.gavvydizzle.rentableregions.utils.Sounds;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.PlayerNameCache;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * Handles all things involving lotteries for shops
 */
public class LotteryManager {

    private static final int SECONDS_REMAINING_FOR_LOTTERY = 600;

    private final Shop shop;
    @NotNull
    private final ArrayList<UUID> lotteryUUIDs;
    private boolean isLotteryActive;

    public LotteryManager(Shop shop, @NotNull ArrayList<UUID> lotteryUUIDs) {
        this.shop = shop;
        this.lotteryUUIDs = lotteryUUIDs;
        setIfLotteryActive();
    }

    /**
     * Updates the flag that determines if this lottery should be active
     */
    public void setIfLotteryActive() {
        isLotteryActive = shop.getRentManager().getSecondsRemaining() != -1 && SECONDS_REMAINING_FOR_LOTTERY >= shop.getRentManager().getSecondsRemaining();
    }

    /**
     * Handles any state of any player trying to join a shop's lottery.
     * Does nothing if a lottery is not active.
     * @param player The player
     * @param enteringLottery true = entering, false = leaving
     */
    public void onLotteryClick(Player player, boolean enteringLottery) {
        if (!isLotteryActive) return;

        if (lotteryUUIDs.contains(player.getUniqueId())) {
            if (enteringLottery) {
                player.sendMessage(Messages.alreadyInLottery);
                Sounds.generalFailSound.playSound(player);
            }
            else {
                lotteryUUIDs.remove(player.getUniqueId());
                RentableRegions.getEconomy().depositPlayer(player, shop.getRentManager().getRentPrice());
                shop.getShopMenu().updateLotteryItem();

                Sounds.lotteryLeaveSound.playSound(player);
                player.sendMessage(Messages.leftLottery.replace("{money}", Numbers.withSuffix(shop.getRentManager().getRentPrice())));
            }
        }
        else if (shop.isMember(player.getUniqueId())) {
            player.sendMessage(Messages.memberEnterLottery);
            Sounds.generalFailSound.playSound(player);
        }
        else if (RentableRegions.getInstance().getShopManager().belongToShop(player)) {
            player.sendMessage(Messages.otherShopMemberEnterLottery);
            Sounds.generalFailSound.playSound(player);
        }
        else if (RentableRegions.getInstance().getShopManager().isInLottery(player)) {
            player.sendMessage(Messages.otherLotteryMemberEnterLottery);
            Sounds.generalFailSound.playSound(player);
        }
        else if (shop.doesNotMeetJoinRequirements(player)) {
            player.sendMessage(Messages.tooLowLevelForLottery.replace("{level}", String.valueOf(shop.getRentManager().getLevelRequired())));
            Sounds.generalFailSound.playSound(player);
        }
        else {
            if (RentableRegions.getEconomy().getBalance(player) < shop.getRentManager().getRentPrice()) {
                player.sendMessage(Messages.tooPoorForLottery);
                Sounds.generalFailSound.playSound(player);
                return;
            }

            lotteryUUIDs.add(player.getUniqueId());
            RentableRegions.getEconomy().withdrawPlayer(player, shop.getRentManager().getRentPrice());
            shop.getShopMenu().updateLotteryItem();

            Sounds.lotteryJoinSound.playSound(player);
            player.sendMessage(Messages.successfulJoinLottery.replace("{money}", Numbers.withSuffix(shop.getRentManager().getRentPrice())));
        }
    }

    /**
     * Handles what happens when the shop expires naturally.
     * This method will end the lottery if one is taking place.
     */
    protected void runLottery() {
        if (lotteryUUIDs.isEmpty()) {
            isLotteryActive = false;
            return;
        }

        UUID winnerUUID = lotteryUUIDs.get(new Random().nextInt(lotteryUUIDs.size()));
        Messages.sendMessage(winnerUUID, Messages.wonLottery);
        Sounds.lotteryWinSound.playSound(winnerUUID);

        lotteryUUIDs.remove(winnerUUID);
        shop.getRentManager().setRentTimeOnNewOwnership();
        shop.setOwner(winnerUUID);

        String message = Messages.lostLottery.replace("{name}", PlayerNameCache.get(winnerUUID));
        for (UUID uuid : lotteryUUIDs) {
            Messages.sendMessage(uuid, message);
            Sounds.lotteryWinSound.playSound(uuid);
        }

        lotteryUUIDs.clear();
        isLotteryActive = false;

        RentableRegions.getInstance().getShopLogger().writeToLog(PlayerNameCache.get(winnerUUID) + " won the lottery for shop " + shop.getId() + ". " +
                "They now have " + Numbers.getTimeFormatted(shop.getRentManager().getSecondsRemaining()) + " remaining");
    }

    /**
     * Cancels the lottery for this shop.
     * If there are player in the lottery, they will receive their money back
     */
    protected void cancelLottery() {
        isLotteryActive = false;

        if (lotteryUUIDs.isEmpty()) {
            return;
        }

        for (UUID uuid : lotteryUUIDs) {
            RentableRegions.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(uuid), shop.getRentManager().getRentPrice());
            Messages.sendMessage(uuid, Messages.cancelOnRent);
        }

        lotteryUUIDs.clear();
    }

    public boolean isInLottery(OfflinePlayer offlinePlayer) {
        return lotteryUUIDs.contains(offlinePlayer.getUniqueId());
    }

    public boolean isLotteryActive() {
        return isLotteryActive;
    }

    public int numParticipants() {
        return lotteryUUIDs.size();
    }

    protected @NotNull ArrayList<String> getLotteryUUIDStrings() {
        ArrayList<String> arr = new ArrayList<>();
        for (UUID uuid : lotteryUUIDs) {
            arr.add(uuid.toString());
        }
        return arr;
    }
}
