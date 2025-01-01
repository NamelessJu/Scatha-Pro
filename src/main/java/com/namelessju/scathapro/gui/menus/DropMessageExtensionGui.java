package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.CycleButton;
import com.namelessju.scathapro.gui.elements.CycleButton.IOptionChangedListener;
import com.namelessju.scathapro.gui.elements.ScathaProLabel;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.enums.DropMessageRarityMode;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class DropMessageExtensionGui extends ScathaProGui
{
    private final Config config;
    
    private ScathaProLabel previewLabel;
    
    public DropMessageExtensionGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
        
        config = scathaPro.getConfig();
    }
    
    @Override
    public String getTitle()
    {
        return "Drop Message Extension Settings";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        IOptionChangedListener<DropMessageRarityMode> rarityModeChangeListener = new CycleButton.IOptionChangedListener<DropMessageRarityMode>() {
            @Override
            public void onChange(CycleButton<DropMessageRarityMode> button) {
                DropMessageRarityMode value = button.getSelectedOption().getOptionValue();
                config.set(Config.Key.dropMessageRarityMode, value != null ? value.name() : "");
                config.save();
            }
        };
        
        elements.add(setGridPosition(previewLabel = new ScathaProLabel(0, 0, 0, 310, "Preview:", Util.Color.GRAY.getValue()).setCentered(), GridElementMode.FULL_WIDTH));
        elements.add(setGridPosition(previewLabel = new ScathaProLabel(0, 0, 0, 310, "[PREVIEW]").setCentered(), GridElementMode.FULL_WIDTH));
        updatePreviewLabel();
        addGridGap();
        addGridButton(new CycleButton<DropMessageRarityMode>(1, 0, 0, 0, 0, "Add Scatha Rarity Text", CycleButton.EnumOption.from(DropMessageRarityMode.class, true), config.getEnum(Config.Key.dropMessageRarityMode, DropMessageRarityMode.class), rarityModeChangeListener), GridElementMode.FULL_WIDTH);
        addGridButton(new BooleanSettingButton(2, 0, 0, 0, 0, "Colored Rarity", Config.Key.dropMessageRarityColored));
        addGridButton(new BooleanSettingButton(3, 0, 0, 0, 0, "Emphasized Rarity", Config.Key.dropMessageRarityUppercase));
        
        addDoneButton();
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);
        
        switch (button.id)
        {
            case 1:
            case 2:
            case 3:
                updatePreviewLabel();
                break;
        }
    }
    
    private void updatePreviewLabel()
    {
        String f = TextUtil.formattingStartCharacter;
        String message = EnumChatFormatting.RESET.toString() + f+"6"+f+"lPET DROP! "+f+"r"+f+"5Scatha "+f+"r"+f+"b"+"(+"+f+"r"+f+"b"+"123% "+f+"r"+f+"b"+"\u272F Magic Find"+f+"r"+f+"b"+")";
        
        IChatComponent extendedPetDropMessage = TextUtil.extendPetDropMessage(message);
        if (extendedPetDropMessage != null) message = extendedPetDropMessage.getFormattedText();
        
        previewLabel.setText(message);
    }
}
