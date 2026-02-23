package namelessju.scathapro.managers;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.miscellaneous.data.enums.ChatCopyButtonMode;
import namelessju.scathapro.miscellaneous.data.enums.DropMessageRarityMode;
import namelessju.scathapro.miscellaneous.data.enums.DropMessageStatMode;
import namelessju.scathapro.miscellaneous.data.enums.Rarity;
import namelessju.scathapro.mixin.ChatComponentAccessor;
import namelessju.scathapro.files.framework.JsonFile;
import namelessju.scathapro.util.SkyblockItemUtil;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.network.chat.*;
import net.minecraft.util.StringDecomposer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ChatManager
{
    public static final Style HIGHLIGHT_STYLE = Style.EMPTY.withColor(ChatFormatting.YELLOW);
    private static final Component CHAT_DIVIDER = Component.empty();
    
    private static final Component CHAT_PREFIX_SHORT = Component.literal("[SP] ").withStyle(ChatFormatting.GRAY);
    private static final Component CHAT_PREFIX_DEV = Component.empty()
        .append(Component.literal("[").withStyle(ChatFormatting.DARK_GREEN))
        .append(Component.literal("[").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.OBFUSCATED))
        .append(Component.literal("Scatha_Dev").withStyle(ChatFormatting.GREEN))
        .append(Component.literal("]").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.OBFUSCATED))
        .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GREEN));
    
    
    private final ScathaPro scathaPro;
    private List<GuiMessage> chatMessages;
    
    private final Queue<Component> cachedChatMessages = new LinkedList<>();
    private final Queue<Component> cachedCrystalHollowsMessages = new LinkedList<>();
    
    public ChatManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public void init()
    {
        chatMessages = ((ChatComponentAccessor) scathaPro.minecraft.gui.getChat()).getMessages();
    }
    
    private Component getChatPrefix()
    {
        return Component.empty()
            .append(Component.literal("[" + scathaPro.getModDisplayName() + "] ").withStyle(ChatFormatting.GRAY))
            .append(Component.empty().withStyle(ChatFormatting.RESET));
    }
    
    public void sendChatMessage(@NonNull String message)
    {
        sendChatMessage(message, true);
    }
    
    public void sendChatMessage(@NonNull String message, boolean addModPrefix)
    {
        sendChatMessage(Component.literal(message), addModPrefix);
    }
    
    public void sendChatMessage(@NonNull Component message)
    {
        sendChatMessage(message, true);
    }
    
    public void sendChatMessage(@NonNull Component message, boolean addModPrefix)
    {
        sendCachableMessage(message, addModPrefix, true,
            cachedChatMessages, "Chat");
    }
    
    public void sendCrystalHollowsMessage(@NonNull Component message)
    {
        sendCachableMessage(message, true, scathaPro.coreManager.isInCrystalHollows(),
            cachedCrystalHollowsMessages, "Crystal Hollows");
    }
    
    private void sendCachableMessage(@NonNull Component message, boolean addModPrefix, boolean sendCondition,
                                     @NonNull Queue<Component> cache, @NonNull String logString)
    {
        if (addModPrefix)
        {
            message = Component.empty()
                .append(
                    scathaPro.config.miscellaneous.shortChatPrefixEnabled.get()
                        ? CHAT_PREFIX_SHORT
                        : getChatPrefix()
                )
                .append(message);
        }
        
        if (!sendCondition || !sendMessageRaw(message))
        {
            cache.add(message);
            ScathaPro.LOGGER.debug("{} message cached: {}", logString, message.getString());
        }
    }
    
    public void sendErrorChatMessage(String errorMessage)
    {
        sendChatMessage(Component.literal(errorMessage).withStyle(ChatFormatting.RED));
    }
    
    public void sendDevChatMessage(String message)
    {
        sendChatMessage(Component.empty().append(CHAT_PREFIX_DEV).append(message), false);
    }
    
    private boolean sendMessageRaw(Component message)
    {
        if (scathaPro.minecraft.player != null)
        {
            scathaPro.minecraft.player.displayClientMessage(message, false);
            return true;
        }
        return false;
    }
    
    public void sendChatDivider()
    {
        if (chatMessages != null && !chatMessages.isEmpty())
        {
            String mostRecentMessageText = chatMessages.getFirst().content().getString();
            if (mostRecentMessageText.equals(CHAT_DIVIDER.getString()))
            {
                return;
            }
        }
        
        sendChatMessage(CHAT_DIVIDER, false);
    }
    
    public @NonNull Component addChatCopyButton(@NonNull Component message)
    {
        ChatCopyButtonMode mode = scathaPro.config.miscellaneous.chatCopyButtonMode.get();
        if (mode == null) return message;
        
        String messageText = StringDecomposer.getPlainText(message);
        if (messageText.isBlank()) return message;
        
        Component chatCopyButtonComponent = mode.buttonComponentBuilder.apply(messageText);
        
        return Component.empty()
            .append(message)
            .append(" ")
            .append(chatCopyButtonComponent);
    }
    
    public void sendCachedMessages()
    {
        while (!cachedChatMessages.isEmpty())
        {
            sendMessageRaw(cachedChatMessages.poll());
        }
    }
    
    public void sendCachedCrystalHollowsMessages()
    {
        while (!cachedCrystalHollowsMessages.isEmpty())
        {
            sendMessageRaw(cachedCrystalHollowsMessages.poll());
        }
    }
    
    /**
     * Checks a message and potentially stops it from being added to chat
     */
    public boolean shouldCancelMessage(@NonNull Component message)
    {
        return scathaPro.config.miscellaneous.hideWormSpawnMessage.get()
            && StringDecomposer.getPlainText(message)
                .equalsIgnoreCase("You hear the sound of something approaching...");
    }
    
    public @NonNull Component onMessageAddedEarly(@NonNull Component message)
    {
        String unformattedText = StringDecomposer.getPlainText(message);
        handleTunnelVisionMessages(unformattedText);
        return replaceMessage(message, unformattedText);
    }
    
    public @NonNull Component onMessageAddedLate(@NonNull Component message)
    {
        return ScathaPro.getInstance().chatManager.addChatCopyButton(message);
    }
    
    private @NonNull Component replaceMessage(@NonNull Component message, @NonNull String unformattedText)
    {
        Component extendedPetDropMessage = extendPetDropMessage(message, unformattedText, true);
        if (extendedPetDropMessage != null) return extendedPetDropMessage;
        
        return message;
    }
    
    private void handleTunnelVisionMessages(@NonNull String unformattedText)
    {
        long now = TimeUtil.now();
        
        if (unformattedText.equalsIgnoreCase("You used your Tunnel Vision Pickaxe Ability!"))
        {
            int cooldown = scathaPro.minecraft.player != null
                ? SkyblockItemUtil.getTunnelVisionCooldown(scathaPro.minecraft.player.getMainHandItem())
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
                    
                    if (spawnCooldownElapsedTime < (long) (Constants.wormSpawnCooldown * 1D/3D))
                    {
                        Achievement.anomalous_desire_waste.unlock();
                    }
                }
            }
        }
        else if (unformattedText.equalsIgnoreCase("Tunnel Vision is now available!"))
        {
            if (scathaPro.coreManager.tunnelVisionCooldownEndTime >= 0L && now - scathaPro.coreManager.tunnelVisionStartTime >= 3000 + Constants.pingTreshold)
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
            if (scathaPro.coreManager.tunnelVisionCooldownEndTime < 0L || (int) Math.abs(scathaPro.coreManager.tunnelVisionCooldownEndTime - newCooldownEndTime) >= Constants.pingTreshold)
            {
                scathaPro.coreManager.tunnelVisionCooldownEndTime = newCooldownEndTime;
                scathaPro.coreManager.tunnelVisionReadyTime = scathaPro.coreManager.tunnelVisionCooldownEndTime;
            }
        }
    }
    
    public Component extendPetDropMessage(@NonNull Component message, @NonNull String unformattedText, boolean clickable)
    {
        if (!unformattedText.equals("PET DROP! Scatha")) return null;
        
        message = TextUtil.convertLegacyFormatting(message);
        Optional<Rarity> foundRarity = message.visit((style, text) -> {
            if (text.contains("Scatha"))
            {
                for (Rarity rarity : Rarity.values())
                {
                    // Check if component style contains the rarity style
                    // (=> if so, component style will be unchanged
                    // when applying the rarity style to it and
                    // hence will still be equal to itself)
                    if (style.equals(rarity.style.applyTo(style)))
                    {
                        return Optional.of(rarity);
                    }
                }
            }
            return Optional.empty();
        }, Style.EMPTY);
        Rarity rarity = foundRarity.orElse(Rarity.UNKNOWN);
        
        MutableComponent newMessage = Component.empty().append(
            Component.literal("PET DROP! ")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
        );
        Component petName = Component.literal("Scatha").setStyle(rarity.style);
        
        // Add rarity text
        
        DropMessageRarityMode rarityMode = scathaPro.config.miscellaneous.dropMessageRarityMode.get();
        if (rarityMode != null)
        {
            String rarityText = rarity.displayName;
            
            MutableComponent rarityComponentRoot = Component.empty().setStyle(
                scathaPro.config.miscellaneous.dropMessageRarityColored.get()
                    ? rarity.style : Style.EMPTY.applyFormats(ChatFormatting.DARK_GRAY)
            );
            Component rarityNameComponent = scathaPro.config.miscellaneous.dropMessageRarityUppercase.get()
                ? Component.literal(rarityText.toUpperCase()).withStyle(ChatFormatting.BOLD)
                : Component.literal(rarityText);
            
            if (rarityMode.hasBrackets) rarityComponentRoot.append("[");
            rarityComponentRoot.append(rarityNameComponent);
            if (rarityMode.hasBrackets) rarityComponentRoot.append("]");
            
            if (rarityMode.isPrefix) petName = Component.empty().append(rarityComponentRoot).append(" ").append(petName);
            else petName = Component.empty().append(petName).append(" ").append(rarityComponentRoot);
        }
        
        newMessage.append(petName);
        
        // Extend Stats
        
        MutableComponent statsComponent = null;
        statsComponent = addPetDropStatComponent(statsComponent,
            scathaPro.config.miscellaneous.dropMessageMagicFindMode,
            scathaPro.persistentDataProfileManager.getMagicFindComponent(true),
            "Magic Find", "MF"
        );
        statsComponent = addPetDropStatComponent(statsComponent,
            scathaPro.config.miscellaneous.dropMessagePetLuckMode,
            scathaPro.persistentDataProfileManager.getPetLuckComponent(true),
            "Pet Luck", "PL"
        );
        statsComponent = addPetDropStatComponent(statsComponent,
            scathaPro.config.miscellaneous.dropMessageEmfMode,
            scathaPro.persistentDataProfileManager.getEffectiveMagicFindComponent(),
            "Effective Magic Find", "EMF"
        );
        if (statsComponent != null)
        {
            Style style = Style.EMPTY.withColor(ChatFormatting.GRAY);
            if (clickable)
            {
                String statsUpdateCommand = "/" + scathaPro.mainCommand.getCommandName() + " profileStats update";
                style = style
                    .withHoverEvent(new HoverEvent.ShowText(
                        Component.literal("Click or use \"" + statsUpdateCommand
                                + "\" to\nset the displayed stat values for future drops")
                        .withStyle(ChatFormatting.GRAY)
                    ))
                    .withClickEvent(new ClickEvent.RunCommand(statsUpdateCommand));
            }
            
            newMessage.append(" ").append(
                Component.empty().setStyle(style).append("(").append(statsComponent).append(")")
            );
        }
        
        // Done
        return newMessage;
    }
    
    private MutableComponent addPetDropStatComponent(@Nullable MutableComponent statsComponent,
                                                     JsonFile.@NonNull PrimitiveValueNullable<DropMessageStatMode> configValue,
                                                     @NonNull MutableComponent valueComponent, @NonNull String fullName,
                                                     @NonNull String abbreviatedName)
    {
        DropMessageStatMode statMode = configValue.get();
        if (statMode == null) return statsComponent;
        
        MutableComponent statComponent = Component.empty();
        
        switch (statMode)
        {
            case FULL_NAME -> valueComponent.append(" " + fullName);
            case SHORT_NAME -> valueComponent.append(" " + abbreviatedName);
            default -> {}
        }
        statComponent.append(valueComponent);
        
        if (statsComponent != null) statsComponent.append(", ");
        else statsComponent = Component.empty();
        return statsComponent.append(statComponent);
    }
}
