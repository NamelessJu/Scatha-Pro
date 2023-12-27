package com.namelessju.scathapro.gui.menus;

import java.util.Arrays;

import com.namelessju.scathapro.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

public class CustomAlertModeGui extends ScathaProGui {

    public String getTitle() {
        return "Custom Alert Mode";
    }

	public CustomAlertModeGui(GuiScreen parentGui) {
		super(parentGui);
	}

    @Override
    public void initGui() {
        super.initGui();
        
        CustomAlertModeManager.instance.loadMeta();
        String currentSubmode = CustomAlertModeManager.instance.getCurrentSubmodeId();
        
        
        GuiLabel currentSubmodeLabel = new GuiLabel(fontRendererObj, 1, width / 2 - 155, 40, 310, 10, Util.Color.GREEN.getValue()).setCentered();
        currentSubmodeLabel.func_175202_a("Current submode: " + CustomAlertModeManager.instance.getSubmodeDisplayName(currentSubmode));
        labelList.add(currentSubmodeLabel);
        
        
        String[] submodes = CustomAlertModeManager.instance.getAllSubmodeIds();
        String[] submodeNames = new String[submodes.length];
        for (int i = 0; i < submodes.length; i ++) {
        	String submodeName = CustomAlertModeManager.instance.getSubmodeDisplayName(submodes[i]);
        	if (submodes[i].equals(currentSubmode)) submodeNames[i] = EnumChatFormatting.GREEN + submodeName + EnumChatFormatting.RESET;
        	else submodeNames[i] = submodeName;
        }
        
        GuiLabel allSubmodesLabel = new GuiLabel(fontRendererObj, 1, width / 2 - 155, 60, 310, 10, Util.Color.WHITE.getValue()).setCentered();
        allSubmodesLabel.func_175202_a("Existing submodes: " + Arrays.toString(submodeNames));
        labelList.add(allSubmodesLabel);
        
        
        buttonList.add(new DoneButton(504704599, width / 2 - 100, height / 6 + 168, 200, 20, "Done", this));
    }
    
}
