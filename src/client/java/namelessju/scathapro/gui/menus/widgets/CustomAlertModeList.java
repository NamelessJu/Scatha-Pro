package namelessju.scathapro.gui.menus.widgets;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertModeManager;
import namelessju.scathapro.files.customalertmode.CustomAlertModeMeta;
import namelessju.scathapro.gui.menus.framework.widgets.lists.ScathaProGuiList;
import namelessju.scathapro.gui.menus.screens.InfoMessageScreen;
import namelessju.scathapro.gui.menus.screens.settings.alerts.customalertmode.CustomAlertModeEditScreen;
import namelessju.scathapro.gui.menus.screens.settings.alerts.customalertmode.CustomAlertModeScreen;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Map;

public class CustomAlertModeList extends ScathaProGuiList
{
    private final ScathaPro scathaPro;
    private final CustomAlertModeScreen screen;

    public CustomAlertModeList(ScathaPro scathaPro, CustomAlertModeScreen screen, HeaderAndFooterLayout layout)
    {
        super(scathaPro.minecraft, screen, layout, 30);
        this.scathaPro = scathaPro;
        this.screen = screen;

        addEntry(new Entry(Button.builder(Component.literal("Create New Custom Alert Mode..."),
            _ -> {
                String newModeId = scathaPro.customAlertModeManager.getNewSubModeId();
                if (newModeId == null)
                {
                    minecraft.gui.setScreen(new InfoMessageScreen(scathaPro, CustomAlertModeList.this.screen,
                        Component.literal("Failed to set up new custom alert mode").withColor(TextColor.RED),
                        Component.literal("Generating a new unique ID failed!\n(Exceeded maximum number of tries)")
                    ));
                    return;
                }
                minecraft.gui.setScreen(new CustomAlertModeEditScreen(scathaPro, CustomAlertModeList.this.screen, newModeId));
            }
        ).bounds(0, 5, getRowWidth(), 20).build()));


        CustomAlertModeManager manager = scathaPro.customAlertModeManager;
        Map<String, CustomAlertModeMeta> allMeta = manager.getAllMeta();

        String[] customModeIds = manager.findAllSubModeIds();
        Arrays.sort(customModeIds, (customModeId1, customModeId2) -> {
            if (manager.isSubModeActive(customModeId1)) return -1;
            if (manager.isSubModeActive(customModeId2)) return 1;

            long lastUsedTime1 = allMeta.get(customModeId1).lastUsedAtTimestamp.getOr(-1L);
            long lastUsedTime2 = allMeta.get(customModeId2).lastUsedAtTimestamp.getOr(-1L);
            return Long.compare(lastUsedTime2, lastUsedTime1);
        });

        for (String customModeId : customModeIds)
        {
            addEntry(createSubModeEntry(customModeId, screen.getFont()));
        }
    }

    private Entry createSubModeEntry(@NonNull String subModeId, Font font)
    {
        Entry entry = new Entry();

        Component modeName = scathaPro.customAlertModeManager.getSubModeDisplayName(subModeId);
        boolean isModeActive = scathaPro.customAlertModeManager.isSubModeActive(subModeId);

        int modeNameWidth = getRowWidth() - 165;
        entry.addPositionedChild(0, 5,
            new StringWidget(modeName, font).setMaxWidth(modeNameWidth, StringWidget.TextOverflow.SCROLLING)
        );

        Component secondLineComponent;
        if (isModeActive)
        {
            secondLineComponent = Component.literal("Selected").withColor(TextColor.GREEN);
        }
        else
        {
            CustomAlertModeMeta meta = scathaPro.customAlertModeManager.subModeMetas.getOrLoad(subModeId);
            long lastUsed = meta.lastUsedAtTimestamp.getOr(-1L);
            secondLineComponent = lastUsed >= 0L
                ? Component.literal(TimeUtil.formatDateTime(scathaPro.config, lastUsed)).withColor(TextColor.DARK_GRAY)
                : Component.literal("Never used").withStyle(Style.EMPTY.withColor(TextColor.DARK_GRAY).withItalic(true));
        }
        entry.addPositionedChild(0, 15, new StringWidget(secondLineComponent, font)
            .setMaxWidth(modeNameWidth, StringWidget.TextOverflow.SCROLLING));

        if (!isModeActive)
        {
            entry.addPositionedChild(getRowWidth() - 160, 5,
                Button.builder(Component.literal("Select"),
                    _ -> {
                        scathaPro.customAlertModeManager.changeSubMode(subModeId);
                        CustomAlertModeList.this.screen.refresh();
                    }
                ).size(50, 20).build()
            );
        }

        entry.addPositionedChild(getRowWidth() - 105, 5,
            Button.builder(Component.literal("Edit..."),
                _ -> minecraft.gui.setScreen(
                    new CustomAlertModeEditScreen(scathaPro, CustomAlertModeList.this.screen, subModeId)
                )
            ).size(50, 20).build()
        );

        entry.addPositionedChild(getRowWidth() - 50, 5,
            new CustomAlertModeDeleteButton(50, 20, scathaPro, CustomAlertModeList.this.screen, subModeId)
        );

        return entry;
    }
}