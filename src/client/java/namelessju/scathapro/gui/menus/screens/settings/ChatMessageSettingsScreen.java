package namelessju.scathapro.gui.menus.screens.settings;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.gui.menus.framework.widgets.lists.TwoColumnGuiList;
import namelessju.scathapro.miscellaneous.data.enums.ChatCopyButtonMode;
import namelessju.scathapro.miscellaneous.data.enums.ChatPrefixType;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.NonNull;

public class ChatMessageSettingsScreen extends ConfigScreen
{
    public ChatMessageSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Chat Message Settings", parentScreen);
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();

        TwoColumnGuiList list = addScrollList();
        list.addDoubleColumn(nullableEnumCycleButton(ChatCopyButtonMode.class,
            "Chat Message Copy Button", config.miscellaneous.chatCopyButtonMode, null,
            _ -> Tooltip.create(
                Component.literal("Adds a clickable icon behind each chat message that copies the message content")
                    .withColor(TextColor.GRAY)
            ), null
        ));
        list.addDoubleColumn(booleanConfigButton("Hide Worm Approaching Message", config.miscellaneous.hideWormSpawnMessage,
            _ -> Tooltip.create(
                Component.literal("Hides Hypixel's chat message that appears when a worm is about to spawn")
                    .withColor(TextColor.GRAY)
            ), null
        ));
        list.addDoubleColumn(booleanConfigButton("Hide Chest Uncovered Message", config.miscellaneous.hideUncoveredChestMessage,
            _ -> Tooltip.create(
                Component.literal("Hides Hypixel's chat message that appears when you uncover a treasure chest")
                    .withColor(TextColor.GRAY)
            ), null
        ));
        list.addDoubleColumn(booleanConfigButton("Compact Chest Lockpick Messages", config.miscellaneous.compactLockpickMessagesEnabled,
            _ -> Tooltip.create(
                Component.literal("Turns Hypixel's chat messages that appear when a chest is lockpicked into a single, one-line message")
                    .withColor(TextColor.GRAY)
            ), null
        ));
        list.addTitle(Component.literal(scathaPro.getModDisplayName() + " Messages"));
        list.addDoubleColumn(enumCycleButton(
                ChatPrefixType.class, scathaPro.getModDisplayName() + " Message Prefix", config.miscellaneous.chatPrefixType,
                value -> Tooltip.create(Component.empty()
                    .append(value.getPrefix(scathaPro))
                    .append(Component.literal("Example message").withColor(TextColor.YELLOW))
                ), null
            ));
        list.addDoubleColumn(booleanConfigButton("Dry Streak Message On Drop", config.miscellaneous.dryStreakMessageEnabled));
        list.addDoubleColumn(booleanConfigButton("Worm Spawn Timer Message", config.miscellaneous.wormSpawnTimerMessageEnabled,
                _ -> Tooltip.create(
                    Component.literal("Sends a message with the elapsed time since the previous worm spawn when you spawn one")
                        .withColor(TextColor.GRAY)
                ), null
            ));
        list.addDoubleColumn(booleanConfigButton("Daily Scatha Farming Streak Messages", config.miscellaneous.dailyStreakMessagesEnabled,
                _ -> Tooltip.create(
                    Component.literal("Sends messages when your daily Scatha farming streak changes")
                        .withColor(TextColor.GRAY)
                ), null
            ));
        list.addDoubleColumn(booleanConfigButton("Saved Value Update Messages", config.miscellaneous.valueUpdatedMessagesEnabled,
            _ -> Tooltip.create(
                Component.literal("Whether to send messages when the mod updates it's stored values by reading them from various UI elements (e.g. worm kills from bestiary)")
                    .withColor(TextColor.GRAY)
            ), null
        ));

        addDoneButtonFooter();
    }
}