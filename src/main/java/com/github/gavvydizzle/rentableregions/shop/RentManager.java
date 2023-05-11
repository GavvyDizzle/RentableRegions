package com.github.gavvydizzle.rentableregions.shop;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.gavvydizzle.rentableregions.utils.Sounds;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.PlayerNameCache;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Handles all things involving renting for shops
 */
public class RentManager {

    private static final int RENT_COOLDOWN = 30;
    private static final int DEFAULT_RENT_PRICE = 1000;
    private static final int DEFAULT_SECONDS_PER_RENT = 1800;
    private static final int DEFAULT_MAX_RENT_SECONDS = 3600;
    private static final int DEFAULT_LEVEL_REQUIREMENT = 0;

    private final Shop shop;
    private int secondsRemaining; // -1 means not rented
    private int secondsPerRent, maxRentSeconds;
    private int rentPrice, levelRequired;

    /**
     * Creates a new RentManager with default values
     * @param shop The shop it belongs to
     */
    public RentManager(Shop shop) {
        this.shop = shop;
        secondsRemaining = -1;
        secondsPerRent = DEFAULT_SECONDS_PER_RENT;
        maxRentSeconds = DEFAULT_MAX_RENT_SECONDS;
        rentPrice = DEFAULT_RENT_PRICE;
        levelRequired = DEFAULT_LEVEL_REQUIREMENT;
    }

    /**
     * Creates a new RentManager with existing values.
     * Sets the time remaining to -1.
     * @param shop The shop it belongs to
     */
    public RentManager(Shop shop, int secondsPerRent, int maxRentSeconds, int rentPrice, int levelRequired) {
        this.shop = shop;
        this.secondsRemaining = -1;
        this.secondsPerRent = secondsPerRent;
        this.maxRentSeconds = maxRentSeconds;
        this.rentPrice = rentPrice;
        this.levelRequired = levelRequired;
    }

    /**
     * Creates a new RentManager with existing values
     * @param shop The shop it belongs to
     */
    public RentManager(Shop shop, int secondsRemaining, int secondsPerRent, int maxRentSeconds, int rentPrice, int levelRequired) {
        this.shop = shop;
        this.secondsRemaining = secondsRemaining;
        this.secondsPerRent = secondsPerRent;
        this.maxRentSeconds = maxRentSeconds;
        this.rentPrice = rentPrice;
        this.levelRequired = levelRequired;
    }

    public void decreaseTime() {
        if (secondsRemaining > 0) {
            secondsRemaining--;
            shop.getLotteryManager().setIfLotteryActive();
            shop.getShopMenu().updateOnTimeDecrease();
        }
        else {
            if (shop.isOccupied()) { // Check for owner because negative times could exist
                shop.messageAllMembers(Messages.outOfTime);
                secondsRemaining = -1;
                shop.onTimeExpire(); // updates all menu items
            }
        }
    }

    /**
     * Handles any state of any player trying to rent or re-rent a shop.
     * Increases time, gives ownership, takes money.
     * @param renter The player trying to rent this shop.
     */
    public void rent(Player renter) {
        double rentCharge = Math.max(0.01, priceOfNextRent());

        // Player attempting to add rent time
        if (shop.isOwnerOrMember(renter.getUniqueId())) {
            if (RentableRegions.getEconomy().getBalance(renter) < rentCharge) {
                renter.sendMessage(Messages.tooPoorOnRentIncrease);
                Sounds.generalFailSound.playSound(renter);
            }
            else if (secondsRemaining > maxRentSeconds - RENT_COOLDOWN) {
                renter.sendMessage(Messages.rentingTooFast);
                Sounds.generalFailSound.playSound(renter);
            }
            else {
                RentableRegions.getEconomy().withdrawPlayer(renter, rentCharge);
                int increaseSeconds = secondsGainedForNextRent();
                secondsRemaining += increaseSeconds;
                shop.getLotteryManager().cancelLottery();

                Sounds.rentSound.playSound(renter);
                renter.sendMessage(Messages.successfulReRent.replace("{money}", Numbers.withSuffix(rentCharge)).replace("{time}", Numbers.getTimeFormatted(increaseSeconds)));
                RentableRegions.getInstance().getShopLogger().writeToLog(renter.getName() + " spent $" + rentCharge + " to increase shop " + shop.getId() + "'s time by " + Numbers.getTimeFormatted(increaseSeconds) + " (to " + Numbers.getTimeFormatted(secondsRemaining) + ")");
            }
            return;
        }

        // From this point onward, the player is not a member of the shop

        // If the shop is owned by someone
        if (shop.isOccupied()) {
            renter.sendMessage(Messages.shopOccupied);
            Sounds.generalFailSound.playSound(renter);
        }
        // If the player belongs to another shop
        else if (RentableRegions.getInstance().getShopManager().belongToShop(renter)) {
            renter.sendMessage(Messages.otherShopMemberRent);
            Sounds.generalFailSound.playSound(renter);
        }
        // If the player is in a lottery, don't let them rent another shop
        else if (RentableRegions.getInstance().getShopManager().isInLottery(renter)) {
            renter.sendMessage(Messages.lotteryMemberOnRent);
            Sounds.generalFailSound.playSound(renter);
        }
        // If the player is too low level to own this shop
        else if (shop.doesNotMeetJoinRequirements(renter)) {
            renter.sendMessage(Messages.tooLowLevelToRent.replace("{level}", String.valueOf(levelRequired)));
            Sounds.generalFailSound.playSound(renter);
        }
        // If the player doesn't have enough money to rent this shop
        else if (RentableRegions.getEconomy().getBalance(renter) < rentCharge) {
            renter.sendMessage(Messages.tooPoorForFirstRent);
            Sounds.generalFailSound.playSound(renter);
        }
        // Otherwise, let them rent the shop and become the owner
        else {
            RentableRegions.getEconomy().withdrawPlayer(renter, rentCharge);
            secondsRemaining = secondsGainedForNextRent(); // Set directly because the secondsRemaining is -1
            shop.setOwner(renter.getUniqueId());
            shop.getShopMenu().updateAllItems();

            Sounds.firstRentSound.playSound(renter);
            renter.sendMessage(Messages.successfulFirstRent.replace("{money}", Numbers.withSuffix(rentCharge)));
            RentableRegions.getInstance().getShopLogger().writeToLog(renter.getName() + " spent $" + rentCharge + " to rent shop " + shop.getId() + " for the initial " + Numbers.getTimeFormatted(secondsRemaining));
        }
    }

    /**
     * @return The price to charge for renting this shop at this moment
     */
    public double priceOfNextRent() {
        if (maxRentSeconds >= secondsRemaining + secondsPerRent) {
            return rentPrice;
        }
        else {
            int secondsToMaxRent = maxRentSeconds - secondsRemaining;
            double pricePerSecond = rentPrice * 1.0 / secondsPerRent;
            return Numbers.round(secondsToMaxRent * pricePerSecond, 2);
        }
    }

    /**
     * Calculates the number of seconds renting will add.
     * Returns the default rent time if the time would not hit the max rent time.
     * @return The number of seconds renting will add
     */
    public int secondsGainedForNextRent() {
        if (maxRentSeconds >= secondsRemaining + secondsPerRent) {
            return secondsPerRent;
        }
        else {
            return maxRentSeconds - secondsRemaining;
        }
    }

    /**
     * Pays the owner the value of the remaining time and resets the time.
     * This should not be called when the time expires naturally.
     * @param oldOwnerUUID The uuid of the old owner
     */
    public void onShopUnclaim(UUID oldOwnerUUID) {
        double pricePerSecond = rentPrice * 1.0 / secondsPerRent;
        double balanceRemaining = Numbers.round(pricePerSecond * secondsRemaining, 2);

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(oldOwnerUUID);
        RentableRegions.getEconomy().depositPlayer(offlinePlayer, balanceRemaining);

        Messages.sendMessage(oldOwnerUUID, Messages.moneyFromUnclaim.replace("{money}", String.valueOf(balanceRemaining)));
        RentableRegions.getInstance().getShopLogger().writeToLog("Shop " + shop.getId() + " has been unclaimed. The old owner (" + PlayerNameCache.get(oldOwnerUUID) + ") has received $" + balanceRemaining + " for the remaining " + Numbers.getTimeFormatted(secondsRemaining) + " of time");

        secondsRemaining = -1;
    }

    /**
     * Sets the number of seconds until this shop runs out of time
     * @param seconds The amount (1 or more)
     */
    public void setTimeRemaining(int seconds) {
        secondsRemaining = Math.max(1, seconds);
    }

    /**
     * Sets the time remaining to -1
     */
    protected void setNoTimeRemaining() {
        secondsRemaining = -1;
    }

    /**
     * Sets the rent time equal to the amount of time of one rent
     */
    protected void setRentTimeOnNewOwnership() {
        secondsRemaining = secondsPerRent;
    }


    //*** GETTERS & SETTERS ***//

    public int getSecondsRemaining() {
        return secondsRemaining;
    }

    public int getSecondsPerRent() {
        return secondsPerRent;
    }

    public void setSecondsPerRent(int secondsPerRent) {
        this.secondsPerRent = secondsPerRent;
    }

    public int getMaxRentSeconds() {
        return maxRentSeconds;
    }

    public void setMaxRentSeconds(int maxRentSeconds) {
        this.maxRentSeconds = maxRentSeconds;
    }

    public int getRentPrice() {
        return rentPrice;
    }

    public void setRentPrice(int rentPrice) {
        this.rentPrice = rentPrice;
    }

    public int getLevelRequired() {
        return levelRequired;
    }

    public void setLevelRequired(int levelRequired) {
        this.levelRequired = levelRequired;
    }
}
