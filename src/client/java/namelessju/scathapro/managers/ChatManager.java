package namelessju.scathapro.managers;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.files.framework.JsonFile;
import namelessju.scathapro.miscellaneous.data.enums.ChatCopyButtonMode;
import namelessju.scathapro.miscellaneous.data.enums.DropMessageRarityMode;
import namelessju.scathapro.miscellaneous.data.enums.DropMessageStatMode;
import namelessju.scathapro.miscellaneous.data.enums.Rarity;
import namelessju.scathapro.mixin.ChatComponentAccessor;
import namelessju.scathapro.util.TextUtil;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.*;
import net.minecraft.util.StringDecomposer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public class ChatManager
{
    public static final Style HIGHLIGHT_STYLE = Style.EMPTY.withColor(TextColor.YELLOW);
    private static final Component CHAT_DIVIDER = Component.empty();

    private static final Component CHAT_PREFIX_DEV = Component.empty().withColor(TextColor.DARK_GREEN)
        .append("[").append(Component.literal("scatha_dev").withColor(TextColor.GREEN)).append("] ");


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
        chatMessages = ((ChatComponentAccessor) scathaPro.minecraft.gui.hud.getChat()).getMessages();
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
        sendCacheableMessage(message, addModPrefix, true,
            cachedChatMessages, "Chat");
    }

    public void sendCrystalHollowsMessage(@NonNull Component message)
    {
        sendCacheableMessage(message, true, scathaPro.coreManager.isInCrystalHollows(),
            cachedCrystalHollowsMessages, "Crystal Hollows");
    }

    private void sendCacheableMessage(@NonNull Component message, boolean addModPrefix, boolean sendCondition,
                                      @NonNull Queue<Component> cache, @NonNull String logString)
    {
        if (addModPrefix)
        {
            message = Component.empty()
                .append(scathaPro.config.miscellaneous.chatPrefixType.get().getPrefix(scathaPro))
                .append(message);
        }

        if (!sendCondition || !sendMessageRaw(message))
        {
            cache.add(message);
            ScathaPro.LOGGER.debug("{} message cached: {}", logString, message.getString());
        }
    }

    public void sendChatErrorMessage(String errorMessage)
    {
        sendChatMessage(Component.literal(errorMessage).withColor(TextColor.RED));
    }

    public void sendDevChatMessage(String message)
    {
        sendDevChatMessage(Component.literal(message));
    }

    public void sendDevChatMessage(Component message)
    {
        sendChatMessage(Component.empty().append(CHAT_PREFIX_DEV).append(message), false);
    }

    public boolean sendMessageRaw(Component message)
    {
        if (scathaPro.minecraft.player != null)
        {
            scathaPro.minecraft.player.sendSystemMessage(message);
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

    public Component extendPetDropMessage(@NonNull Component message, @NonNull String unformattedText, boolean allowShuriken, boolean isClickable)
    {
        if (!unformattedText.equals(Constants.petDropMessageRaw)) return null;

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
            Component.literal("PET DROP! ").withStyle(Style.EMPTY.withColor(TextColor.GOLD).withBold(true))
        );
        Component petName = Component.literal("Scatha").setStyle(rarity.style);

        // Add rarity text

        DropMessageRarityMode rarityMode = scathaPro.config.miscellaneous.dropMessageRarityMode.get();
        if (rarityMode != null)
        {
            String rarityText = rarity.rarityName;

            MutableComponent rarityComponentRoot = Component.empty().setStyle(
                scathaPro.config.miscellaneous.dropMessageRarityColored.get()
                    ? rarity.style : Style.EMPTY.withColor(TextColor.DARK_GRAY)
            );
            Component rarityNameComponent = scathaPro.config.miscellaneous.dropMessageRarityUppercase.get()
                ? Component.literal(rarityText.toUpperCase()).withStyle(Style.EMPTY.withBold(true))
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
            scathaPro.persistentDataProfileManager.getTotalMagicFindComponent(allowShuriken, true),
            "Magic Find", "MF"
        );
        statsComponent = addPetDropStatComponent(statsComponent,
            scathaPro.config.miscellaneous.dropMessagePetLuckMode,
            scathaPro.persistentDataProfileManager.getPetLuckComponent(true),
            "Pet Luck", "PL"
        );
        statsComponent = addPetDropStatComponent(statsComponent,
            scathaPro.config.miscellaneous.dropMessageEmfMode,
            scathaPro.persistentDataProfileManager.getEffectiveMagicFindComponent(allowShuriken),
            "Effective Magic Find", "EMF"
        );
        if (statsComponent != null)
        {
            String statsUpdateCommand = "/" + scathaPro.mainCommand.getCommandName() + " profileStats";
            Style statsStyle = Style.EMPTY.withColor(TextColor.GRAY);
            if (isClickable)
            {
                statsStyle = statsStyle
                    .withHoverEvent(new HoverEvent.ShowText(
                        Component.literal("Click or use \"" + statsUpdateCommand
                                + "\" to\nset the displayed stat values for future drops")
                            .withColor(TextColor.GRAY)
                    ))
                    .withClickEvent(new ClickEvent.RunCommand(statsUpdateCommand));
            }
            newMessage.append(" ").append(
                Component.empty().setStyle(statsStyle).append("(").append(statsComponent).append(")")
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