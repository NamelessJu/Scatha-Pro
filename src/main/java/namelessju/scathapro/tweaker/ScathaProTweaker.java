package namelessju.scathapro.tweaker;

import java.io.File;
import java.util.List;

import net.hypixel.modapi.tweaker.HypixelModAPITweaker;
import org.spongepowered.asm.launch.MixinTweaker;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;

public class ScathaProTweaker implements ITweaker
{
    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile)
    {
        @SuppressWarnings({"unchecked"})
        List<String> tweakClassNames = (List<String>) Launch.blackboard.get("TweakClasses");
        tweakClassNames.add(MixinTweaker.class.getName());
        tweakClassNames.add(ModLoadingTweaker.class.getName());
        tweakClassNames.add(HypixelModAPITweaker.class.getName());
        
        // TODO: temp fix, figure out how to properly prevent
        // both the tweaker result and the shadowed dependency
        // from loading together in the dev environment
        CoreModManager.getIgnoredMods().add("mod-api-forge-1.0.1.2.jar");
    }
    
    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader)
    {
        
    }
    
    @Override
    public String getLaunchTarget()
    {
        return null;
    }
    
    @Override
    public String[] getLaunchArguments()
    {
        return new String[0];
    }
}
