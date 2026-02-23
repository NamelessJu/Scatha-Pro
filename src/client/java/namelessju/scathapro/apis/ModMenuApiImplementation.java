package namelessju.scathapro.apis;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.screens.settings.MainSettingsScreen;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuApiImplementation implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory()
    {
        return parent -> new MainSettingsScreen(ScathaPro.getInstance(), parent);
    }
}
