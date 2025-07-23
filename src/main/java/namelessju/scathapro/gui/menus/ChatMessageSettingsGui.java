package namelessju.scathapro.gui.menus;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.elements.BooleanSettingButton;
import namelessju.scathapro.managers.Config;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

public class ChatMessageSettingsGui extends ConfigGui
{
    public ChatMessageSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    @Override
    public String getTitle()
    {
        return "Chat Message Settings";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        addGridButton(new BooleanSettingButton(7, 0, 0, 0, 0, "Short " + ScathaPro.DYNAMIC_MODNAME + " Message Prefix", Config.Key.shortChatPrefix), GridElementMode.FULL_WIDTH);
        
        addGridButton(new BooleanSettingButton(1, 0, 0, 0, 0, "Dry Streak Message On Drop", Config.Key.dryStreakMessage), GridElementMode.FULL_WIDTH);
        
        BooleanSettingButton wormSpawnTimerMessageButton = new BooleanSettingButton(2, 0, 0, 0, 0, "Worm Spawn Timer Message", Config.Key.wormSpawnTimer);
        wormSpawnTimerMessageButton.getTooltip().setText(EnumChatFormatting.GRAY + "Sends a message with the elapsed time since the previous worm spawn when you spawn one");
        addGridButton(wormSpawnTimerMessageButton, GridElementMode.FULL_WIDTH);
        
        BooleanSettingButton dailyStreakMessageButton = new BooleanSettingButton(8, 0, 0, 0, 0, "Daily Scatha Farming Streak Messages", Config.Key.dailyScathaFarmingStreakMessage);
        dailyStreakMessageButton.getTooltip().setText(EnumChatFormatting.GRAY + "Sends messages when your daily\nScatha farming streak changes");
        addGridButton(dailyStreakMessageButton, GridElementMode.FULL_WIDTH);
        
        BooleanSettingButton chatCopySettingButton = new BooleanSettingButton(6, 0, 0, 0, 0, "Chat Message Copy Button", Config.Key.chatCopy);
        chatCopySettingButton.getTooltip().setText(EnumChatFormatting.GRAY + "Adds a clickable icon behind each chat message that copies the message into the input field");
        addGridButton(chatCopySettingButton, GridElementMode.FULL_WIDTH);
        
        BooleanSettingButton hideWormApproachingMessageButton = new BooleanSettingButton(12, 0, 0, 0, 0, "Hide Worm Approaching Message", Config.Key.hideWormSpawnMessage);
        hideWormApproachingMessageButton.getTooltip().setText(EnumChatFormatting.GRAY + "Hides Hypixel's chat message that appears when a worm is about to spawn");
        addGridButton(hideWormApproachingMessageButton, GridElementMode.FULL_WIDTH);
        
        
        addDoneButton();
    }
}
