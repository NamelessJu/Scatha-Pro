package namelessju.scathapro.gui.menus.widgets;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.widgets.lists.ScathaProGuiList;
import namelessju.scathapro.gui.menus.screens.InfoMessageScreen;
import namelessju.scathapro.gui.menus.screens.settings.alerts.customalertmode.CustomAlertModeEditScreen;
import namelessju.scathapro.gui.menus.screens.settings.alerts.customalertmode.CustomAlertModeScreen;
import namelessju.scathapro.managers.CustomAlertModeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

// TODO: can be put into screen class
public class CustomAlertModeList extends ScathaProGuiList
{
    private final ScathaPro scathaPro;
    private final CustomAlertModeScreen screen;
    
    public CustomAlertModeList(ScathaPro scathaPro, CustomAlertModeScreen screen, HeaderAndFooterLayout layout)
    {
        super(scathaPro.minecraft, screen, layout, 30);
        this.scathaPro = scathaPro;
        this.screen = screen;
        
        Button btn;
        addEntry(new Entry(btn = Button.builder(Component.literal("Create New Custom Alert Mode..."),
            button -> {
                String newModeId = scathaPro.customAlertModeManager.getNewSubModeId();
                if (newModeId == null)
                {
                    minecraft.setScreen(new InfoMessageScreen(scathaPro, CustomAlertModeList.this.screen,
                        Component.literal("Failed to set up new custom alert mode").withStyle(ChatFormatting.RED),
                        Component.literal("Generating a new unique ID failed!\n(Exceeded maximum number of tries)")
                    ));
                    return;
                }
                minecraft.setScreen(new CustomAlertModeEditScreen(scathaPro, CustomAlertModeList.this.screen, newModeId));
            }
        ).bounds(0, 5, getRowWidth(), 20).build()));
        btn.active = false;
        btn.setTooltip(Tooltip.create(Component.literal("Work in Progress").withStyle(ChatFormatting.YELLOW)));
        
        
        CustomAlertModeManager manager = scathaPro.customAlertModeManager;
        manager.loadAllMeta();
        
        String[] customModeIds = manager.findAllSubModeIds();
        Arrays.sort(customModeIds, (customModeId1, customModeId2) -> {
            long lastUsedTime1 = manager.getSubModeLastUsed(customModeId1);
            long lastUsedTime2 = manager.getSubModeLastUsed(customModeId2);
            
            if (manager.isSubModeActive(customModeId2)) return 1;
            if (manager.isSubModeActive(customModeId1)) return -1;
            
            return Long.compare(lastUsedTime2, lastUsedTime1);
        });
        
        for (String customModeId : customModeIds)
        {
            addEntry(createSubModeEntry(customModeId, screen.getFont()));
        }
    }
    
    private Entry createSubModeEntry(String subModeId, Font font)
    {
        Entry entry = new Entry();
        
        Component modeName = scathaPro.customAlertModeManager.getSubModeDisplayName(subModeId);
        boolean isModeActive = scathaPro.customAlertModeManager.isSubModeActive(subModeId);
        
        int modeNameWidth = getRowWidth() - 165;
        entry.addPositionedChild(0, (isModeActive ? 5 : 10),
            new StringWidget(modeName, font).setMaxWidth(modeNameWidth, StringWidget.TextOverflow.SCROLLING)
        );
        
        if (isModeActive) entry.addPositionedChild(0, 15,
            new StringWidget(Component.literal("Selected").withStyle(ChatFormatting.GREEN), font)
                .setMaxWidth(modeNameWidth)
        );
        
        Button selectButton;
        entry.addPositionedChild(getRowWidth() - 160, 5,
            selectButton = Button.builder(Component.literal("Select"),
                button -> {
                    scathaPro.customAlertModeManager.changeSubMode(subModeId);
                    CustomAlertModeList.this.screen.refresh();
                }
            ).size(50, 20).build()
        );
        selectButton.active = !isModeActive;
        
        Button btn;
        entry.addPositionedChild(getRowWidth() - 105, 5,
            btn = Button.builder(Component.literal("Edit..."),
                button -> minecraft.setScreen(
                    new CustomAlertModeEditScreen(scathaPro, CustomAlertModeList.this.screen, subModeId)
                )
            ).size(50, 20).build()
        );
        btn.active = false;
        btn.setTooltip(Tooltip.create(Component.literal("Work in Progress").withStyle(ChatFormatting.YELLOW)));
        
        entry.addPositionedChild(getRowWidth() - 50, 5,
            new CustomAlertModeDeleteButton(50, 20, scathaPro, CustomAlertModeList.this.screen, subModeId)
        );
        
        return entry;
    }
}
