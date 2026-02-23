package namelessju.scathapro.gui.menus.screens;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.LayoutScreen;
import namelessju.scathapro.gui.menus.screens.settings.AchievementSettingsScreen;
import namelessju.scathapro.gui.menus.widgets.AchievementsList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class AchievementListScreen extends LayoutScreen
{
    private AchievementsList achievementsList = null;
    
    public AchievementListScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, Component.literal("Achievements"), true, parentScreen);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();
        
        layout.addToContents(
            achievementsList = new AchievementsList(0, 0, width, 0, scathaPro),
            LayoutSettings::alignHorizontallyCenter
        );
        
        LinearLayout footerLayout = LinearLayout.vertical().spacing(4);
        footerLayout.addChild(
            subScreenButtonBuilder(Component.literal("Achievement Settings"),
                () -> new AchievementSettingsScreen(scathaPro, this)
            ).width(200).build(),
            LayoutSettings::alignHorizontallyCenter
        );
        footerLayout.addChild(doneButton(), LayoutSettings::alignHorizontallyCenter);
        addLayoutFooter(footerLayout);
    }
    
    private void updateListSize()
    {
        if (achievementsList == null) return;
        
        achievementsList.setWidth(width);
        int topPadding = 13;
        achievementsList.setY(getLayout().getHeaderHeight() + topPadding);
        achievementsList.setHeight(getLayout().getContentHeight() - topPadding);
    }
    
    @Override
    public void added()
    {
        super.added();
        
        if (achievementsList != null)
        {
            achievementsList.added();
        }
    }
    
    @Override
    protected void repositionElements()
    {
        super.repositionElements();
        updateListSize();
    }
}
