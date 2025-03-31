package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.CycleButton;
import com.namelessju.scathapro.gui.elements.CycleButton.IOptionChangedListener;
import com.namelessju.scathapro.gui.elements.ScathaProLabel;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.enums.DropMessageRarityMode;
import com.namelessju.scathapro.miscellaneous.enums.DropMessageStatsMode;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.UnicodeSymbol;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class DropMessageExtensionGui extends ScathaProGui
{
    private final Config config;
    
    private ScathaProLabel previewLabel;
    private ScathaProLabel statsUpdateInfoLabel;
    private CycleButton<DropMessageRarityMode> rarityModeButton;
    private BooleanSettingButton coloredRarityButton, emphasizedRarityButton;
    private CycleButton<DropMessageStatsMode> statsModeButton;
    private BooleanSettingButton cleanMagicFindButton;
    
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
                DropMessageRarityMode value = button.getSelectedValue();
                config.set(Config.Key.dropMessageRarityMode, value != null ? value.name() : "");
                config.save();
            }
        };
        IOptionChangedListener<DropMessageStatsMode> statsModeChangeListener = new CycleButton.IOptionChangedListener<DropMessageStatsMode>() {
            @Override
            public void onChange(CycleButton<DropMessageStatsMode> button) {
                DropMessageStatsMode value = button.getSelectedValue();
                config.set(Config.Key.dropMessageStatsMode, value != null ? value.name() : "");
                config.save();
                
                updateStatsModeButton();
            }
        };
        
        elements.add(setGridPosition(previewLabel = new ScathaProLabel(0, 0, 0, 310, "[PREVIEW]").setCentered(), GridElementMode.FULL_WIDTH));
        elements.add(setGridPosition(statsUpdateInfoLabel = new ScathaProLabel(0, 0, 0, 310, "Use \"/sp profileStats\" to update\nthe used Pet Luck and EMF values", Util.Color.GRAY).setCentered(), GridElementMode.FULL_WIDTH));
        
        addGridGap();
        
        addGridButton(rarityModeButton = new CycleButton<DropMessageRarityMode>(1, 0, 0, 0, 0, "Add Scatha Rarity Text", CycleButton.EnumOption.from(DropMessageRarityMode.class, true), config.getEnum(Config.Key.dropMessageRarityMode, DropMessageRarityMode.class), rarityModeChangeListener), GridElementMode.FULL_WIDTH);
        addGridButton(coloredRarityButton = new BooleanSettingButton(2, 0, 0, 0, 0, "Colored Rarity", Config.Key.dropMessageRarityColored));
        addGridButton(emphasizedRarityButton = new BooleanSettingButton(3, 0, 0, 0, 0, "Emphasized Rarity", Config.Key.dropMessageRarityUppercase));
        
        addGridGap();
        
        addGridButton(statsModeButton = new CycleButton<DropMessageStatsMode>(4, 0, 0, 0, 0, "Modify Stats", CycleButton.EnumOption.from(DropMessageStatsMode.class, true), config.getEnum(Config.Key.dropMessageStatsMode, DropMessageStatsMode.class), statsModeChangeListener), GridElementMode.FULL_WIDTH);
        BooleanSettingButton abbreviatedStatNamesButton;
        addGridButton(abbreviatedStatNamesButton = new BooleanSettingButton(5, 0, 0, 0, 0, "Abbreviated Stat Names", Config.Key.dropMessageStatAbbreviations));
        abbreviatedStatNamesButton.getTooltip().setText(EnumChatFormatting.GRAY + "Doesn't apply to EMF");
        addGridButton(cleanMagicFindButton = new BooleanSettingButton(6, 0, 0, 0, 0, "Cleaner Magic Find", Config.Key.dropMessageCleanMagicFind));
        
        addDoneButton();
        
        updatePreviewLabel();
        updateRaritySettingButtons();
        updateStatsUpdateInfoLabel();
        updateStatsModeButton();
        updateCleanMagicFindButton();
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);
        
        switch (button.id)
        {
            case 1:
                updateRaritySettingButtons();
            case 2:
            case 3:
                updatePreviewLabel();
                break;
            
            case 4:
                updateStatsUpdateInfoLabel();
                updateCleanMagicFindButton();
            case 5:
            case 6:
                updatePreviewLabel();
                break;
        }
    }
    
    private void updatePreviewLabel()
    {
        String f = TextUtil.formattingStartCharacter;
        String message = EnumChatFormatting.RESET.toString() + f+"6"+f+"lPET DROP! "+f+"r"+f+"5Scatha "+f+"r"+f+"b"+"(+"+f+"r"+f+"b"+"123% "+f+"r"+f+"b"+UnicodeSymbol.magicFind+" Magic Find"+f+"r"+f+"b"+")";
        
        IChatComponent extendedPetDropMessage = TextUtil.extendPetDropMessage(message);
        if (extendedPetDropMessage != null) message = extendedPetDropMessage.getFormattedText();
        
        previewLabel.setText(message);
    }
    
    private void updateStatsUpdateInfoLabel()
    {
        DropMessageStatsMode statsMode = statsModeButton.getSelectedValue();
        statsUpdateInfoLabel.visible = statsMode != null && (statsMode.showPetLuck || statsMode.emfMode != null);
    }
    
    private void updateRaritySettingButtons()
    {
        if (rarityModeButton.getSelectedValue() != null)
        {
            coloredRarityButton.enabled = true;
            coloredRarityButton.getTooltip().setText(null);
            
            emphasizedRarityButton.enabled = true;
            emphasizedRarityButton.getTooltip().setText(null);
        }
        else
        {
            String tooltip = EnumChatFormatting.YELLOW + "Applies only when the\nrarity text is added";
            
            coloredRarityButton.enabled = false;
            coloredRarityButton.getTooltip().setText(tooltip);
            
            emphasizedRarityButton.enabled = false;
            emphasizedRarityButton.getTooltip().setText(tooltip);
        }
    }
    
    private void updateStatsModeButton()
    {
        DropMessageStatsMode statsMode = statsModeButton.getSelectedValue();
        if (statsMode != null && statsMode.emfMode != null)
        {
            statsModeButton.getTooltip().setText(EnumChatFormatting.GRAY + "EMF (\"Effective Magic Find\"):\nMagic Find + Pet Luck combined");
        }
        else statsModeButton.getTooltip().setText("");
    }
    
    private void updateCleanMagicFindButton()
    {
        DropMessageStatsMode statsMode = statsModeButton.getSelectedValue();
        if (statsMode == null || statsMode.showMagicFind)
        {
            cleanMagicFindButton.enabled = true;
            cleanMagicFindButton.getTooltip().setText(null);
        }
        else
        {
            cleanMagicFindButton.enabled = false;
            cleanMagicFindButton.getTooltip().setText(EnumChatFormatting.YELLOW + "Applies only when\nMagic Find is shown");
        }
    }
}
