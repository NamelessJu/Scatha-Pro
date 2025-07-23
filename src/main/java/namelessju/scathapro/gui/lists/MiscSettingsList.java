package namelessju.scathapro.gui.lists;

import namelessju.scathapro.GlobalVariables;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.elements.BooleanSettingButton;
import namelessju.scathapro.gui.elements.ScappaModeButton;
import namelessju.scathapro.gui.elements.ScathaProButton;
import namelessju.scathapro.gui.elements.ScathaProLabel;
import namelessju.scathapro.gui.menus.ScathaProGui;
import namelessju.scathapro.managers.Config;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.util.EnumChatFormatting;

public class MiscSettingsList extends ScathaProGuiList
{
    private BooleanSettingButton googlyEyesButton = null;
    
    public MiscSettingsList(ScathaProGui parentGui)
    {
        super(parentGui, 24);
        
        GlobalVariables variables = ScathaPro.getInstance().variables;
        
        BooleanSettingButton parseBestiaryButton = new BooleanSettingButton(5, 0, 0, 0, 0, "Automatic Bestiary Parsing", Config.Key.automaticWormStatsParsing);
        parseBestiaryButton.getTooltip().setText(EnumChatFormatting.GRAY + "Automatically reads kills and bestiary Magic Find from the worm bestiary menu");
        addButton(parseBestiaryButton, true);
        
        addButton(new BooleanSettingButton(13, 0, 0, 0, 0, "Automatic Pet Drop Chat Screenshot", Config.Key.automaticPetDropScreenshot), true);
        
        BooleanSettingButton highContrastColorsButton = new BooleanSettingButton(9, 0, 0, 0, 0, "High Contrast Colors", Config.Key.highContrastColors);
        highContrastColorsButton.getTooltip().setText(EnumChatFormatting.GRAY + "Turns gray overlay\nand title text to white");
        addButton(highContrastColorsButton, true);

        if (variables.lastAprilFoolsJokeShownYear >= 0)
        {
            addButton(new BooleanSettingButton(15, 0, 0, 0, 0, "April Fools Fake Pet Drop", Config.Key.aprilFoolsFakeDropEnabled), true);
        }
        
        addCategory("Unlockables");
        
        if (variables.scappaModeUnlocked)
        {
            ScappaModeButton scappaModeButton = new ScappaModeButton(11, 0, 0, 0, 0);
            scappaModeButton.getTooltip().setText(TextUtil.getRainbowText("Scappa"));
            addButton(scappaModeButton, true);
        }
        if (variables.overlayIconGooglyEyesUnlocked)
        {
            googlyEyesButton = new BooleanSettingButton(14, 0, 0, 0, 0, "Overlay Icon Googly Eyes", Config.Key.overlayIconGooglyEyes);
            updateGooglyEyesButtonEnabled();
            addButton(googlyEyesButton, true);
        }
        
        removeEmptyCategory();
    }
    
    private void updateGooglyEyesButtonEnabled()
    {
        if (googlyEyesButton == null) return;
        
        if (TimeUtil.isAprilFools)
        {
            googlyEyesButton.enabled = false;
            googlyEyesButton.getTooltip().setText(EnumChatFormatting.YELLOW + "Googly eyes are always\nenabled on april fools day");
            return;
        }
        if (ScathaPro.getInstance().isScappaModeActive())
        {
            googlyEyesButton.enabled = false;
            googlyEyesButton.getTooltip().setText(EnumChatFormatting.YELLOW + "Googly eyes don't apply\nto the Scappa mode icon");
            return;
        }
        
        googlyEyesButton.enabled = true;
        googlyEyesButton.getTooltip().setText(null);
    }
    
    /*
    private void addButton(ScathaProButton button)
    {
        addButton(button, false);
    }
    */
    
    private void addButton(ScathaProButton button, boolean fullWidth)
    {
        ListEntry lastEntry = getLastEntry();
        ButtonEntry lastButtonEntry = lastEntry instanceof ButtonEntry ? (ButtonEntry) lastEntry : null;
        if (fullWidth || lastButtonEntry == null || lastButtonEntry.isFull)
        {
            lastButtonEntry = new ButtonEntry(button, fullWidth);
            listEntries.add(lastButtonEntry);
        }
        else
        {
            lastButtonEntry.addButton(button);
        }
    }
    
    private void addCategory(String text)
    {
        removeEmptyCategory();
        listEntries.add(new CategoryEntry(text));
    }
    
    private void removeEmptyCategory()
    {
        ListEntry lastEntry = getLastEntry();
        if (lastEntry != null && lastEntry instanceof CategoryEntry)
        {
            listEntries.remove(listEntries.size() - 1);
        }
    }
    
    private ListEntry getLastEntry()
    {
        int listSize = listEntries.size();
        if (listSize <= 0) return null;
        return listEntries.get(listSize - 1);
    }
    
    
    private class CategoryEntry extends ListEntry
    {
        public CategoryEntry(String text)
        {
            addElement(new ScathaProLabel(0, 0, slotHeight / 2 - MiscSettingsList.this.mc.fontRendererObj.FONT_HEIGHT / 2, MiscSettingsList.this.getListWidth(), text).setCentered());
        }
    }
    
    private class ButtonEntry extends ListEntry
    {
        private boolean isFull = false;
        
        private ButtonEntry(ScathaProButton leftButton, boolean fullWidth)
        {
            leftButton.setElementX(0);
            leftButton.setElementY(0);
            leftButton.setElementWidth(fullWidth ? 310 : 150);
            leftButton.setElementHeight(20);
            addElement(leftButton);
            
            isFull = fullWidth;
        }
        
        public void addButton(ScathaProButton rightButton)
        {
            if (isFull) return;
            rightButton.setElementX(160);
            rightButton.setElementY(0);
            rightButton.setElementWidth(150);
            rightButton.setElementHeight(20);
            addElement(rightButton);
            
            isFull = true;
        }
        
        protected void onButtonPressed(ScathaProButton button)
        {
            switch (button.id)
            {
                case 9:
                    ScathaPro.getInstance().getOverlay().updateContrast();
                    break;
                case 11:
                    updateGooglyEyesButtonEnabled();
                    break;
                case 14:
                    ScathaPro.getInstance().getOverlay().updateGooglyEyesEnabled();
                    break;
            }
        }
    }
}
