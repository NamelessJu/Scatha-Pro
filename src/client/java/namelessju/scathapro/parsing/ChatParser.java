package namelessju.scathapro.parsing;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.miscellaneous.data.enums.WitchesStew;
import namelessju.scathapro.util.SkyBlockItemUtil;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.TimeUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.StringDecomposer;
import org.jspecify.annotations.NonNull;

import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatParser
{
    private final ScathaPro scathaPro;

    private final Pattern witchesStewEatenPattern = Pattern.compile("^EW! You ate an? (.+?)\\.");
    private final Pattern scathaShardsCaughtPattern = Pattern.compile("^You caught (?:a|x\\d+) Scatha Shards?!$");
    private final Pattern stonewormCharmedPattern = Pattern.compile("^CHARM! You charmed the Stoneworm and received \\d+ Stoneworm Shards?!$");

    public ChatParser(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }

    /**
     * Checks a message and potentially stops it from being added to chat
     */
    public boolean shouldCancelMessage(@NonNull Component message)
    {
        String unformattedText = StringDecomposer.getPlainText(message);

        if (scathaPro.config.miscellaneous.hideWormSpawnMessage.get()
            && unformattedText.equalsIgnoreCase("You hear the sound of something approaching..."))
            return true;

        if (scathaPro.coreManager.hasPendingBlackHoleKill() && scathaShardsCaughtPattern.matcher(unformattedText).find())
        {
            scathaPro.coreManager.triggerBlackHoleKill();
        }

        if (scathaPro.scathaDropsSlotMachineManager.shouldHideDrops())
        {
            scathaPro.scathaDropsSlotMachineManager.addDelayedChatMessage(message);
            return true;
        }

        return false;
    }

    public @NonNull Component beforeMessageAddedEarly(@NonNull Component message)
    {
        String unformattedText = StringDecomposer.getPlainText(message);
        parseMessage(unformattedText);
        return replaceMessage(message, unformattedText);
    }

    public @NonNull Component beforeMessageAddedLate(@NonNull Component message)
    {
        return scathaPro.chatManager.addChatCopyButton(message);
    }

    private void parseMessage(@NonNull String unformattedText)
    {
        Matcher matcher;

        if ((matcher = witchesStewEatenPattern.matcher(unformattedText)).find())
        {
            ScathaPro.LOGGER.debug("Detected stew eaten message");
            String stewString = matcher.group(1);
            if (stewString != null)
            {
                ScathaPro.LOGGER.debug(" -> contains stew name");
                for (WitchesStew stew : WitchesStew.values())
                {
                    if (stew.stewName.equalsIgnoreCase(stewString))
                    {
                        PersistentData.ProfileData profileData = scathaPro.getProfileData();
                        int previousMagicFind = profileData.witchesStewsEaten.getMagicFind();

                        profileData.witchesStewsEaten.setEaten(stew, true);
                        scathaPro.persistentData.save();
                        scathaPro.mainOverlay.updateProfileStats();
                        ScathaPro.LOGGER.debug(" -> stew {} detected and marked as eaten", stew.name());

                        int newMagicFind = profileData.witchesStewsEaten.getMagicFind();
                        if (newMagicFind != previousMagicFind)
                        {
                            scathaPro.runNextTick(() -> scathaPro.chatManager.sendChatMessage(Component.empty().withColor(TextColor.GRAY)
                                .append("Updated Witches Stew Scatha Magic Find (")
                                .append(TextUtil.numberToComponentOrObf(previousMagicFind, 2, false, RoundingMode.HALF_UP))
                                .append(" " + UnicodeSymbol.hypixelArrowRight + " ")
                                .append(TextUtil.numberToComponentOrObf(newMagicFind, 2, false, RoundingMode.HALF_UP))
                                .append(")")
                            ));
                        }
                        break;
                    }
                }
            }
            return;
        }

        if (stonewormCharmedPattern.matcher(unformattedText).find())
        {
            Achievement.stoneworm_charm.unlock();
        }

        parseTunnelVisionMessages(unformattedText);
    }

    private @NonNull Component replaceMessage(@NonNull Component original, @NonNull String unformattedText)
    {
        Component extendedPetDropMessage = scathaPro.chatManager.extendPetDropMessage(original, unformattedText, true, true);
        if (extendedPetDropMessage != null) return extendedPetDropMessage;

        return original;
    }

    private void parseTunnelVisionMessages(@NonNull String unformattedText)
    {
        long now = TimeUtil.getEpochMilliseconds();

        if (unformattedText.equalsIgnoreCase("You used your Tunnel Vision Pickaxe Ability!"))
        {
            int cooldown = scathaPro.minecraft.player != null
                ? SkyBlockItemUtil.getTunnelVisionCooldown(scathaPro.minecraft.player.getMainHandItem())
                : -1;
            if (cooldown >= 0)
            {
                scathaPro.coreManager.tunnelVisionCooldownEndTime = now + cooldown * 1000L;
                scathaPro.coreManager.tunnelVisionReadyTime = scathaPro.coreManager.tunnelVisionCooldownEndTime;
            }

            scathaPro.coreManager.tunnelVisionWastedForRecovery = false;
            scathaPro.coreManager.tunnelVisionStartTime = now;

            if (scathaPro.coreManager.wormSpawnCooldownStartTime >= 0L)
            {
                long spawnCooldownElapsedTime = now - scathaPro.coreManager.wormSpawnCooldownStartTime;
                if (spawnCooldownElapsedTime < (long) (Constants.wormSpawnCooldown * 0.5D))
                {
                    scathaPro.coreManager.tunnelVisionWastedForRecovery = true;

                    if (spawnCooldownElapsedTime < (long) (Constants.wormSpawnCooldown / 3D))
                    {
                        Achievement.anomalous_desire_waste.unlock();
                    }
                }
            }
        }
        else if (unformattedText.equalsIgnoreCase("Tunnel Vision is now available!"))
        {
            if (scathaPro.coreManager.tunnelVisionCooldownEndTime >= 0L
                && now - scathaPro.coreManager.tunnelVisionStartTime >= Constants.tunnelVisionEffectDuration)
            {
                scathaPro.coreManager.tunnelVisionReadyTime = now;
                scathaPro.coreManager.tunnelVisionCooldownEndTime = -1L;
                scathaPro.coreManager.tunnelVisionWastedForRecovery = false;
                scathaPro.coreManager.tunnelVisionStartTime = -1L;
            }
        }
        else if (unformattedText.toLowerCase().startsWith("your pickaxe ability is on cooldown for "))
        {
            String cooldownNumberString = unformattedText.substring(40);
            if (cooldownNumberString.endsWith(".")) cooldownNumberString = cooldownNumberString.substring(0, cooldownNumberString.length() - 1);
            cooldownNumberString = cooldownNumberString.trim().substring(0, cooldownNumberString.length() - 1); // remove "s"
            Integer cooldownRemainingSeconds = TextUtil.parseInt(cooldownNumberString);

            if (cooldownRemainingSeconds == null) return;
            long newCooldownEndTime = now + cooldownRemainingSeconds * 1000L;
            if (scathaPro.coreManager.tunnelVisionCooldownEndTime < 0L || (int) Math.abs(scathaPro.coreManager.tunnelVisionCooldownEndTime - newCooldownEndTime) >= Constants.pingThreshold)
            {
                scathaPro.coreManager.tunnelVisionCooldownEndTime = newCooldownEndTime;
                scathaPro.coreManager.tunnelVisionReadyTime = scathaPro.coreManager.tunnelVisionCooldownEndTime;
            }
        }
    }
}