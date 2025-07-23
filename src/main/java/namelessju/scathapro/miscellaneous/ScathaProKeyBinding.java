package namelessju.scathapro.miscellaneous;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.settings.KeyBinding;

public class ScathaProKeyBinding extends KeyBinding
{
    public static enum Category
    {
        MAIN("main"),
        PLAYER_ROTATION("playerRotation"),
        PLAYER_CONTROLS("playerControls"),
        SCREENSHOTS("screenshots");
        
        private final String categoryKey;
        
        Category(String categoryKey)
        {
            this.categoryKey = categoryKey;
        }
        
        public String getCategoryID()
        {
            return "key.categories." + ScathaPro.MODID + "." + categoryKey;
        }
        
        public String getShortNameTranslationKey()
        {
            return getCategoryID() + ".short";
        }
    }
    
    
    public final Category category;

    public ScathaProKeyBinding(String id, int defaultKey, Category category)
    {
        super("key." + ScathaPro.MODID + "." + id, defaultKey, category.getCategoryID());
        this.category = category;
    }
}
