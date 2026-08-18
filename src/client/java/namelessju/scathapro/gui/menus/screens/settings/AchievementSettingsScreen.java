package namelessju.scathapro.gui.menus.screens.settings;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class AchievementSettingsScreen extends ConfigScreen
{
    private CycleButton<Boolean> repeatUnlockAlertsButton;

    public AchievementSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Achievement Settings", parentScreen);
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();

        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addSingleCell(booleanConfigButton("Unlock Alerts", config.achievements.playAlerts, null,
            (_, _) -> updateRepeatUnlockAlertsButton()
        ));
        gridBuilder.addSingleCell(repeatUnlockAlertsButton = booleanConfigButton(
            "Repeated Unlock Alerts", config.achievements.playRepeatAlerts,
            _ -> Tooltip.create(
                config.achievements.playAlerts.get()
                    ? Component.literal("Whether to play unlock alerts for achievements getting repeated").withStyle(ChatFormatting.GRAY)
                    : Component.literal("Applies only when achievement unlock alerts are enabled in general").withStyle(ChatFormatting.YELLOW)
            ), null
        ));
        gridBuilder.addGap();
        gridBuilder.addFullWidth(booleanConfigButton("Pre-Open List Categories", config.achievements.listPreOpenCategories,
            _ -> Tooltip.create(
                Component.literal("Automatically opens all categories when opening the achievement list").withStyle(ChatFormatting.GRAY)
            ), null
        ));
        gridBuilder.addFullWidth(booleanConfigButton("Show Non-Unlocked Bonus Achievements", config.achievements.listShowBonusAchievements));
        gridBuilder.addFullWidth(booleanConfigButton("Hide Unlocked Achievements", config.achievements.listHideUnlockedAchievements));
        gridBuilder.addFullWidth(booleanConfigButton("Show Repeat Counts And Progress", config.achievements.listShowRepeatCounts));
        gridBuilder.addToContent(layout);

        addDoneButtonFooter();

        updateRepeatUnlockAlertsButton();
    }

    private void updateRepeatUnlockAlertsButton()
    {
        repeatUnlockAlertsButton.active = config.achievements.playAlerts.get();
        // update tooltip by reinserting the current value
        // (thanks Mojang for making updateTooltip() private...)
        repeatUnlockAlertsButton.setValue(repeatUnlockAlertsButton.getValue());
    }
}