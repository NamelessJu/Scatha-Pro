package namelessju.scathapro.gui.lists;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import namelessju.scathapro.gui.elements.ScathaProButton;
import namelessju.scathapro.gui.elements.ScathaProLabel;
import namelessju.scathapro.gui.menus.KeybindingsGui;
import namelessju.scathapro.miscellaneous.ScathaProKeyBinding;
import namelessju.scathapro.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;

public class KeybindingsGuiList extends ScathaProGuiList
{
    private final Minecraft mc;
    private final KeybindingsGui keybindingsGui;
    private int maxListLabelWidth = 0;
    
    public KeybindingsGuiList(KeybindingsGui keybindingsGui)
    {
        super(keybindingsGui, 25);
        
        this.keybindingsGui = keybindingsGui;
        this.mc = keybindingsGui.mc;
        
        List<ScathaProKeyBinding> scathaProKeybindings = Lists.newArrayList();
        for (KeyBinding keybinding : mc.gameSettings.keyBindings)
        {
            if (keybinding instanceof ScathaProKeyBinding) scathaProKeybindings.add((ScathaProKeyBinding) keybinding);
        }
        ScathaProKeyBinding[] keybindings = scathaProKeybindings.toArray(new ScathaProKeyBinding[0]);
        Arrays.sort(keybindings);
        
        for (ScathaProKeyBinding keybinding : keybindings)
        {
            int nameWidth = TextUtil.getStringWidth(I18n.format(keybinding.getKeyDescription()));
            if (nameWidth > maxListLabelWidth)
            {
                maxListLabelWidth = nameWidth;
            }
        }
        
        ScathaProKeyBinding.Category currentCategory = null;
        for (ScathaProKeyBinding keybinding : keybindings)
        {
            ScathaProKeyBinding.Category category = keybinding.category;
            if (category != currentCategory)
            {
                if (category != ScathaProKeyBinding.Category.MAIN)
                {
                    listEntries.add(new CategoryEntry(category.getShortNameTranslationKey()));
                }
                currentCategory = category;
            }
            
            listEntries.add(new KeyEntry(keybinding));
        }
        
        listEntries.add(new AllKeybindingsButtonEntry());
        listEntries.add(new SpacingEntry());
    }

    private class AllKeybindingsButtonEntry extends ListEntry
    {
        public AllKeybindingsButtonEntry()
        {
            addElement(new ScathaProButton(1, getListWidth() / 2 - 75, slotHeight - 20, 150, 20, "All Key Bindings..."));
        }
        
        @Override
        protected void onButtonPressed(ScathaProButton button)
        {
            switch (button.id)
            {
                case 1:
                    mc.displayGuiScreen(new GuiControls(gui, mc.gameSettings));
                    break;
            }
        }
    }

    public class CategoryEntry extends ListEntry
    {
        public CategoryEntry(String category)
        {
            addElement(new ScathaProLabel(0, 0, slotHeight / 2 - KeybindingsGuiList.this.mc.fontRendererObj.FONT_HEIGHT / 2, KeybindingsGuiList.this.getListWidth(), I18n.format(category)).setCentered());
        }
    }
    
    private class KeyEntry extends ListEntry
    {
        private final ScathaProKeyBinding keyBinding;
        private final ScathaProButton buttonChangeKeybinding;
        private final ScathaProButton buttonReset;
        
        public KeyEntry(ScathaProKeyBinding keyBinding)
        {
            this.keyBinding = keyBinding;
            
            int endPosition = getListWidth() - (Math.max(getListWidth() - (145 + maxListLabelWidth), 0) / 2);
            
            addElement(new ScathaProLabel(0, endPosition - 145 - maxListLabelWidth, 10 - mc.fontRendererObj.FONT_HEIGHT / 2, maxListLabelWidth, I18n.format(keyBinding.getKeyDescription())));
            addElement(buttonChangeKeybinding = new ScathaProButton(1, endPosition - 130, 0, 75, 20, ""));
            addElement(buttonReset = new ScathaProButton(2, endPosition - 50, 0, 50, 20, I18n.format("controls.reset")));
        }
        
        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
        {
            buttonReset.enabled = keyBinding.getKeyCode() != keyBinding.getKeyCodeDefault();
            
            String keyString = GameSettings.getKeyDisplayString(keyBinding.getKeyCode());
            
            if (keybindingsGui.changingKeybinding == keyBinding)
            {
                buttonChangeKeybinding.displayString = EnumChatFormatting.WHITE + "> " + EnumChatFormatting.YELLOW + keyString + EnumChatFormatting.WHITE + " <";
            }
            else
            {
                boolean isDuplicate = false;
                if (keyBinding.getKeyCode() != 0)
                {
                    for (KeyBinding otherKeybinding : mc.gameSettings.keyBindings)
                    {
                        if (otherKeybinding != keyBinding && keyBinding.getKeyCode() == otherKeybinding.getKeyCode())
                        {
                            isDuplicate = true;
                            break;
                        }
                    }
                }
                
                if (isDuplicate)
                {
                    buttonChangeKeybinding.displayString = EnumChatFormatting.RED + keyString;
                }
                else
                {
                    buttonChangeKeybinding.displayString = keyString;
                }
            }
            
            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);
        }
        
        @Override
        protected void onButtonPressed(ScathaProButton button)
        {
            switch (button.id)
            {
                case 1:
                    keybindingsGui.changingKeybinding = keyBinding;
                    break;
                
                case 2:
                    mc.gameSettings.setOptionKeyBinding(keyBinding, keyBinding.getKeyCodeDefault());
                    KeyBinding.resetKeyBindingArrayAndHash();
                    break;
            }
        }
    }
    
    public class SpacingEntry extends ListEntry
    {
        // it's so empty in here
    }
}
