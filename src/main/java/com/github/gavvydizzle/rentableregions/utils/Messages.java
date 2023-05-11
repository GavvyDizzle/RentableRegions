package com.github.gavvydizzle.rentableregions.utils;

import com.github.gavvydizzle.rentableregions.configs.MessagesConfig;
import com.github.mittenmc.serverutils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Messages {

    /**
     * Attempts to message the player by their uuid
     * @param uuid The player's uuid
     * @param messages The message(s) to send.
     */
    public static void sendMessage(@Nullable UUID uuid, String... messages) {
        if (uuid == null) return;

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.sendMessage(messages);
        }
    }

    // Ownership
    public static String addedAsMember, removedAsMember, invalidMemberOnSwap, newOwnerOnSwap, oldOwnerOnSwap, successfulKick;

    // Renting
    public static String outOfTime, moneyFromUnclaim, successfulFirstRent,
            tooPoorOnRentIncrease, rentingTooFast, successfulReRent, shopOccupied, otherShopMemberRent, lotteryMemberOnRent, tooLowLevelToRent, tooPoorForFirstRent;

    // Lottery
    public static String wonLottery, lostLottery, cancelOnRent, successfulJoinLottery,
            alreadyInLottery, leftLottery, memberEnterLottery, otherShopMemberEnterLottery, otherLotteryMemberEnterLottery, tooLowLevelForLottery, tooPoorForLottery;

    // Other actions
    public static String shopTransferred, givenShopByTransfer, selfShopPublic, selfShopPrivate, visitorsRemoved, errorRemovingVisitors;

    // Invites
    public static String inviteSent, inviteReceived, sentInviteAccepted, receivedInviteAccepted, sentInviteExpired, receivedInviteExpired, sentInviteCancelled, receivedInviteCancelled, noPendingInvite, maxMembersReached;
    public static String noOutstandingInvite, otherOutstandingInvite;

    // Trading
    public static String creatorConfirmation, creatorSentTrade, recipientConfirmation;
    public static String moneyMustBePositive, notEnoughTimeRemaining, selfNotEnoughTimeRemaining, notEnoughMoney, selfNotEnoughMoney, recipientDoesNotOwnShop, noLongerOwner, selfNoLongerOwner;
    public static String hasOutstandingTrade, selfHasOutstandingTrade, tradeConfirmationExpired, tradeExpired, selfTradeExpired, successfulTradeGiveMoney, successfulTradeReceiveMoney, messageMembersOnSuccessfulTrade;
    public static String buyTradeCreator, buyTradeRecipient, sellTradeCreator, sellTradeRecipient, swapTradeCreator_give, swapTradeRecipient_give, swapTradeCreator_request, swapTradeRecipient_request;

    // Errors
    public static String invalidVisitLocation, playerBelongsToShop, doesNotBelongToShop, selfDoesNotBelongToShop, doesNotBelongToSelfShop, isNotOwnerOfShop, noOnlinePlayerFound, noPlayerFound, invalidShopID;
    public static String doesNotMeetRequirements, selfDoesNotMeetRequirements, ownerTriedToLeave, ownerKickSelf, ownerPromoteSelf, shopSignPermissionDenied, tooLowLevelToInvite;

    public static void reloadMessages() {
        FileConfiguration config = MessagesConfig.get();
        config.options().copyDefaults(true);

        // Ownership
        config.addDefault("addedAsMember", "&aYou are now a member of {name}'s shop");
        config.addDefault("removedAsMember", "&eYou are no longer a member of {name}'s shop");
        config.addDefault("invalidMemberOnSwap", "&cYou cannot swap ownership because this player is not a member of your shop");
        config.addDefault("newOwnerOnSwap", "&a{name} promoted you to the owner of their shop");
        config.addDefault("oldOwnerOnSwap", "&aSuccessfully demoted yourself to a member and made {name} the new owner");
        config.addDefault("successfulKick", "&aYou successfully kicked {name} from your shop");

        // Renting
        config.addDefault("outOfTime", "&cYour shop ran out of time");
        config.addDefault("moneyFromUnclaim", "&aYou received ${money} from the rent time remaining");
        config.addDefault("successfulFirstRent", "&aYou have been charged ${money} to claim this shop");
        config.addDefault("tooPoorOnRentIncrease", "&cYou don't have enough money to increase rent");
        config.addDefault("rentingTooFast", "&cPlease wait before renting again");
        config.addDefault("successfulReRent", "&eYou have been charged ${money} to increase the rent time by {time}");
        config.addDefault("shopOccupied", "&cThis shop already has an owner");
        config.addDefault("otherShopMemberRent", "&cYou already belong to a shop");
        config.addDefault("lotteryMemberOnRent", "&cYou cannot rent a shop when entered in a shop lottery");
        config.addDefault("tooLowLevelToRent", "&cYou need to be at least level {level} to rent this shop");
        config.addDefault("tooPoorForFirstRent", "&cYou don't have enough money to rent this shop");

        // Lottery
        config.addDefault("wonLottery", "&aCONGRATULATIONS! You won the shop lottery!");
        config.addDefault("lostLottery", "&e{name} won the lottery! Better luck next time");
        config.addDefault("cancelOnRent", "&eThe lottery was cancelled because the shop was re-rented. Your money has been refunded");
        config.addDefault("successfulJoinLottery", "&aYou were charged ${money} to enter this lottery");
        config.addDefault("alreadyInLottery", "&cYou are already entered in this lottery. Right-click to leave it");
        config.addDefault("leftLottery", "&eYou received ${money} from leaving this lottery");
        config.addDefault("memberEnterLottery", "&cYou can't join your own shop's lottery");
        config.addDefault("otherShopMemberEnterLottery", "&cYou can't enter a lottery if you are a member of another shop");
        config.addDefault("otherLotteryMemberEnterLottery", "&cYou can only enter one shop lottery at a time");
        config.addDefault("tooLowLevelForLottery", "&cYou need to be at least level {level} to join this lottery");
        config.addDefault("tooPoorForLottery", "&cYou don't have enough money to enter this lottery");

        // Other actions
        config.addDefault("shopTransferred", "&eYour shop has been transferred to another player");
        config.addDefault("givenShopByTransfer", "&aYou were given ownership of shop {id}");
        config.addDefault("selfShopPublic", "&aYour shop is now public");
        config.addDefault("selfShopPrivate", "&eYour shop is now private");
        config.addDefault("visitorsRemoved", "&aSuccessfully removed all visitors from your shop");
        config.addDefault("errorRemovingVisitors", "&cFailed to remove visitors because your shop has no visit location");

        // Invites
        config.addDefault("inviteSent", "&aSuccessfully sent an invite to {name}");
        config.addDefault("inviteReceived", "&e(!) {name} is inviting you to join shop {id}. Click here to join");
        config.addDefault("sentInviteAccepted", "&aYour invite has been accepted");
        config.addDefault("receivedInviteAccepted", "&aInvite accepted. You are now a member of shop {id}");
        config.addDefault("sentInviteExpired", "&cYour invite to {name} has expired");
        config.addDefault("receivedInviteExpired", "&cYour invite from {name} has expired");
        config.addDefault("sentInviteCancelled", "&cCancelled your invite to {name}");
        config.addDefault("receivedInviteCancelled", "&cYour invite from {name} has been cancelled");
        config.addDefault("noPendingInvite", "&cYou do not have a pending invite");
        config.addDefault("maxMembersReached", "&cThe shop is at maximum player capacity");
        config.addDefault("noOutstandingInvite", "&cYou do not have an outstanding invite");
        config.addDefault("otherOutstandingInvite", "&cYou cannot invite this player because they have an outstanding invite");

        // Trading
        config.addDefault("creatorConfirmation", "&f[Shop Trade] &eClick here to send the trade");
        config.addDefault("creatorSentTrade", "&f[Shop Trade] &cTrade offer sent");
        config.addDefault("recipientConfirmation", "&f[Shop Trade] &cClick here to accept the trade");
        config.addDefault("moneyMustBePositive", "&f[Shop Trade] &cA trade must have a positive money amount");
        config.addDefault("notEnoughTimeRemaining", "&f[Shop Trade] &c{name}'s shop does not have enough time remaining to trade it. It must have at least {time}");
        config.addDefault("selfNotEnoughTimeRemaining", "&f[Shop Trade] &cYour shop does not have enough time remaining to trade it. It must have at least {time}");
        config.addDefault("notEnoughMoney", "&f[Shop Trade] &c{name} does not have enough money to complete this trade");
        config.addDefault("selfNotEnoughMoney", "&f[Shop Trade] &cYou do not have enough money to complete this trade");
        config.addDefault("recipientDoesNotOwnShop", "&f[Shop Trade] &c{name} must own a shop to complete this trade");
        config.addDefault("noLongerOwner", "&f[Shop Trade] &c{name} is no longer owner of their shop");
        config.addDefault("selfNoLongerOwner", "&f[Shop Trade] &cYou are no longer owner of your shop");
        config.addDefault("hasOutstandingTrade", "&f[Shop Trade] &c{name} already has an active trade");
        config.addDefault("selfHasOutstandingTrade", "&f[Shop Trade] &cYou already have an active trade");
        config.addDefault("tradeConfirmationExpired", "&f[Shop Trade] &cYour trade confirmation expired");
        config.addDefault("tradeExpired", "&f[Shop Trade] &cYour trade request with {name} expired");
        config.addDefault("selfTradeExpired", "&f[Shop Trade] &cYour trade request from {name} expired");
        config.addDefault("successfulTradeGiveMoney", "&f[Shop Trade] &aTrade successful. You were charged ${money}");
        config.addDefault("successfulTradeReceiveMoney", "&f[Shop Trade] &aTrade successful. You received ${money}");
        config.addDefault("messageMembersOnSuccessfulTrade", "&f[Shop Trade] &e{name} traded your shop to {name2}");
        config.addDefault("buyTradeCreator", "&f[Shop Trade] &eYou will buy {name}'s shop for ${money} (shop {id})");
        config.addDefault("buyTradeRecipient", "&f[Shop Trade] &eYou will sell your shop to {name} for ${money}");
        config.addDefault("sellTradeCreator", "&f[Shop Trade] &eYou will sell your shop to {name} for ${money}");
        config.addDefault("sellTradeRecipient", "&f[Shop Trade] &eYou will buy {name}'s shop for ${money} (shop {id})");
        config.addDefault("swapTradeCreator_give", "&f[Shop Trade] &eYou will swap shops with {name} and pay them ${money} (shop {id})");
        config.addDefault("swapTradeRecipient_give", "&f[Shop Trade] &eYou will swap shops with {name} and receive ${money} (shop {id})");
        config.addDefault("swapTradeCreator_request", "&f[Shop Trade] &eYou will swap shops with {name} and receive ${money} (shop {id})");
        config.addDefault("swapTradeRecipient_request", "&f[Shop Trade] &eYou will swap shops with {name} and pay them ${money} (shop {id})");

        // Errors
        config.addDefault("invalidVisitLocation", "&cNo visit location exists for shop {id}");
        config.addDefault("playerBelongsToShop", "&c{name} already belongs to a shop");
        config.addDefault("doesNotBelongToShop", "&c{name} does not belong to any shop");
        config.addDefault("selfDoesNotBelongToShop", "&cYou must belong to a shop to use this command");
        config.addDefault("doesNotBelongToSelfShop", "&c{name} player is not a member of your shop");
        config.addDefault("isNotOwnerOfShop", "&cOnly shop owners can do this");
        config.addDefault("noOnlinePlayerFound", "&cNo online player found with the name `{name}`");
        config.addDefault("noPlayerFound", "&cNo player found with the name `{name}`");
        config.addDefault("invalidShopID", "&cNo shop exists with the id '{id}'");
        config.addDefault("doesNotMeetRequirements", "&c{name} is not high enough level for your shop");
        config.addDefault("selfDoesNotMeetRequirements", "&cYou are not high enough level for {name}'s shop");
        config.addDefault("ownerTriedToLeave", "&cYou cannot leave your shop as the owner");
        config.addDefault("ownerKickSelf", "&cYou cannot kick yourself");
        config.addDefault("ownerPromoteSelf", "&aYou cannot promote yourself");
        config.addDefault("shopSignPermissionDenied", "&cYou don't have permission to use shop signs");
        config.addDefault("tooLowLevelToInvite", "&c{name}'s level is too low to join your shop");

        MessagesConfig.save();

        // Ownership
        addedAsMember = Colors.conv(config.getString("addedAsMember"));
        removedAsMember = Colors.conv(config.getString("removedAsMember"));
        invalidMemberOnSwap = Colors.conv(config.getString("invalidMemberOnSwap"));
        newOwnerOnSwap = Colors.conv(config.getString("newOwnerOnSwap"));
        oldOwnerOnSwap = Colors.conv(config.getString("oldOwnerOnSwap"));
        successfulKick = Colors.conv(config.getString("successfulKick"));

        // Renting
        outOfTime = Colors.conv(config.getString("outOfTime"));
        moneyFromUnclaim = Colors.conv(config.getString("moneyFromUnclaim"));
        successfulFirstRent = Colors.conv(config.getString("successfulFirstRent"));
        tooPoorOnRentIncrease = Colors.conv(config.getString("tooPoorOnRentIncrease"));
        rentingTooFast = Colors.conv(config.getString("rentingTooFast"));
        successfulReRent = Colors.conv(config.getString("successfulReRent"));
        shopOccupied = Colors.conv(config.getString("shopOccupied"));
        otherShopMemberRent = Colors.conv(config.getString("otherShopMemberRent"));
        lotteryMemberOnRent = Colors.conv(config.getString("lotteryMemberOnRent"));
        tooLowLevelToRent = Colors.conv(config.getString("tooLowLevelToRent"));
        tooPoorForFirstRent = Colors.conv(config.getString("tooPoorForFirstRent"));

        // Lottery
        wonLottery = Colors.conv(config.getString("wonLottery"));
        lostLottery = Colors.conv(config.getString("lostLottery"));
        cancelOnRent = Colors.conv(config.getString("cancelOnRent"));
        successfulJoinLottery = Colors.conv(config.getString("successfulJoinLottery"));
        alreadyInLottery = Colors.conv(config.getString("alreadyInLottery"));
        leftLottery = Colors.conv(config.getString("leftLottery"));
        memberEnterLottery = Colors.conv(config.getString("memberEnterLottery"));
        otherShopMemberEnterLottery = Colors.conv(config.getString("otherShopMemberEnterLottery"));
        otherLotteryMemberEnterLottery = Colors.conv(config.getString("otherLotteryMemberEnterLottery"));
        tooLowLevelForLottery = Colors.conv(config.getString("tooLowLevelForLottery"));
        tooPoorForLottery = Colors.conv(config.getString("tooPoorForLottery"));

        // Other actions
        shopTransferred = Colors.conv(config.getString("shopTransferred"));
        givenShopByTransfer = Colors.conv(config.getString("givenShopByTransfer"));
        selfShopPublic = Colors.conv(config.getString("selfShopPublic"));
        selfShopPrivate = Colors.conv(config.getString("selfShopPrivate"));
        visitorsRemoved = Colors.conv(config.getString("visitorsRemoved"));
        errorRemovingVisitors = Colors.conv(config.getString("errorRemovingVisitors"));

        // Invites
        inviteSent = Colors.conv(config.getString("inviteSent"));
        inviteReceived = Colors.conv(config.getString("inviteReceived"));
        sentInviteAccepted = Colors.conv(config.getString("sentInviteAccepted"));
        receivedInviteAccepted = Colors.conv(config.getString("receivedInviteAccepted"));
        sentInviteExpired = Colors.conv(config.getString("sentInviteExpired"));
        receivedInviteExpired = Colors.conv(config.getString("receivedInviteExpired"));
        sentInviteCancelled = Colors.conv(config.getString("sentInviteCancelled"));
        receivedInviteCancelled = Colors.conv(config.getString("receivedInviteCancelled"));
        noPendingInvite = Colors.conv(config.getString("noPendingInvite"));
        maxMembersReached = Colors.conv(config.getString("maxMembersReached"));
        noOutstandingInvite = Colors.conv(config.getString("noOutstandingInvite"));
        otherOutstandingInvite = Colors.conv(config.getString("otherOutstandingInvite"));

        // Trading
        creatorConfirmation = Colors.conv(config.getString("creatorConfirmation"));
        creatorSentTrade = Colors.conv(config.getString("creatorSentTrade"));
        recipientConfirmation = Colors.conv(config.getString("recipientConfirmation"));
        moneyMustBePositive = Colors.conv(config.getString("moneyMustBePositive"));
        notEnoughTimeRemaining = Colors.conv(config.getString("notEnoughTimeRemaining"));
        selfNotEnoughTimeRemaining = Colors.conv(config.getString("selfNotEnoughTimeRemaining"));
        notEnoughMoney = Colors.conv(config.getString("notEnoughMoney"));
        selfNotEnoughMoney = Colors.conv(config.getString("selfNotEnoughMoney"));
        recipientDoesNotOwnShop = Colors.conv(config.getString("recipientDoesNotOwnShop"));
        noLongerOwner = Colors.conv(config.getString("noLongerOwner"));
        selfNoLongerOwner = Colors.conv(config.getString("selfNoLongerOwner"));
        hasOutstandingTrade = Colors.conv(config.getString("hasOutstandingTrade"));
        selfHasOutstandingTrade = Colors.conv(config.getString("selfHasOutstandingTrade"));
        tradeConfirmationExpired = Colors.conv(config.getString("tradeConfirmationExpired"));
        tradeExpired = Colors.conv(config.getString("tradeExpired"));
        selfTradeExpired = Colors.conv(config.getString("selfTradeExpired"));
        successfulTradeGiveMoney = Colors.conv(config.getString("successfulTradeGiveMoney"));
        successfulTradeReceiveMoney = Colors.conv(config.getString("successfulTradeReceiveMoney"));
        messageMembersOnSuccessfulTrade = Colors.conv(config.getString("messageMembersOnSuccessfulTrade"));
        buyTradeCreator = Colors.conv(config.getString("buyTradeCreator"));
        buyTradeRecipient = Colors.conv(config.getString("buyTradeRecipient"));
        sellTradeCreator = Colors.conv(config.getString("sellTradeCreator"));
        sellTradeRecipient = Colors.conv(config.getString("sellTradeRecipient"));
        swapTradeCreator_give = Colors.conv(config.getString("swapTradeCreator_give"));
        swapTradeRecipient_give = Colors.conv(config.getString("swapTradeRecipient_give"));
        swapTradeCreator_request = Colors.conv(config.getString("swapTradeCreator_request"));
        swapTradeRecipient_request = Colors.conv(config.getString("swapTradeRecipient_request"));

        // Errors
        invalidVisitLocation = Colors.conv(config.getString("invalidVisitLocation"));
        playerBelongsToShop = Colors.conv(config.getString("playerBelongsToShop"));
        doesNotBelongToShop = Colors.conv(config.getString("doesNotBelongToShop"));
        selfDoesNotBelongToShop = Colors.conv(config.getString("selfDoesNotBelongToShop"));
        doesNotBelongToSelfShop = Colors.conv(config.getString("doesNotBelongToSelfShop"));
        isNotOwnerOfShop = Colors.conv(config.getString("isNotOwnerOfShop"));
        noOnlinePlayerFound = Colors.conv(config.getString("noOnlinePlayerFound"));
        noPlayerFound = Colors.conv(config.getString("noPlayerFound"));
        invalidShopID = Colors.conv(config.getString("invalidShopID"));
        doesNotMeetRequirements = Colors.conv(config.getString("doesNotMeetRequirements"));
        selfDoesNotMeetRequirements = Colors.conv(config.getString("selfDoesNotMeetRequirements"));
        ownerTriedToLeave = Colors.conv(config.getString("ownerTriedToLeave"));
        ownerKickSelf = Colors.conv(config.getString("ownerKickSelf"));
        ownerPromoteSelf = Colors.conv(config.getString("ownerPromoteSelf"));
        shopSignPermissionDenied = Colors.conv(config.getString("shopSignPermissionDenied"));
        tooLowLevelToInvite = Colors.conv(config.getString("tooLowLevelToInvite"));
    }
}
