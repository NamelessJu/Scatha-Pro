package namelessju.scathapro.gui.menus.screens.settings;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.miscellaneous.data.enums.ChatCopyButtonMode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
        
        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addFullWidth(booleanConfigButton("Short " + scathaPro.getModDisplayName() + " Message Prefix", config.miscellaneous.shortChatPrefixEnabled));
        gridBuilder.addFullWidth(booleanConfigButton("Dry Streak Message On Drop", config.miscellaneous.dryStreakMessageEnabled));
        gridBuilder.addFullWidth(booleanConfigButton("Worm Spawn Timer Message", config.miscellaneous.wormSpawnTimerMessageEnabled,
                value -> Tooltip.create(
                    Component.literal("Sends a message with the elapsed time since the previous worm spawn when you spawn one")
                        .withStyle(ChatFormatting.GRAY)
                ), null
            ));
        gridBuilder.addFullWidth(booleanConfigButton("Daily Scatha Farming Streak Messages", config.miscellaneous.dailyStreakMessagesEnabled,
                value -> Tooltip.create(
                    Component.literal("Sends messages when your daily Scatha farming streak changes")
                        .withStyle(ChatFormatting.GRAY)
                ), null
            ));
        gridBuilder.addFullWidth(nullableEnumCycleButton(ChatCopyButtonMode.class,
            "Chat Message Copy Button", config.miscellaneous.chatCopyButtonMode, null,
                value -> Tooltip.create(
                    Component.literal("Adds a clickable icon behind each chat message that copies the message content")
                        .withStyle(ChatFormatting.GRAY)
                ), null
            ));
        gridBuilder.addFullWidth(booleanConfigButton("Hide Worm Approaching Message", config.miscellaneous.hideWormSpawnMessage,
                value -> Tooltip.create(
                    Component.literal("Hides Hypixel's chat message that appears when a worm is about to spawn")
                        .withStyle(ChatFormatting.GRAY)
                ), null
            ));
        gridBuilder.addToContent(layout);
        
        addDoneButtonFooter();
    }
}
