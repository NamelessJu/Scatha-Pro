package namelessju.scathapro.parsing;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.gui.menus.screens.FakeBanScreen;
import namelessju.scathapro.sounds.instances.ScathaProSound;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

public class SoundParser
{
    private final ScathaPro scathaPro;

    public SoundParser(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }

    public boolean handlePlaySound(@NonNull SoundInstance sound)
    {
        // Mute sounds in fake ban screen
        if (scathaPro.minecraft.screen instanceof FakeBanScreen
            && !isVanillaSound(sound, "ui.button.click"))
        {
            return false;
        }

        // Mute sounds in Scatha drops slot machine
        if (scathaPro.scathaDropsSlotMachineManager.shouldHideDrops()
            && !(sound instanceof ScathaProSound))
        {
            return false;
        }


        if (!scathaPro.coreManager.isInCrystalHollows()) return true;


        // Detect worm pre-spawn
        long now = TimeUtil.getEpochMilliseconds();
        if (
            (scathaPro.coreManager.lastPreAlertTime < 0L || now - scathaPro.coreManager.lastPreAlertTime > 10000)
                && isVanillaSound(sound, "entity.spider.step")
                && (
                Mth.equal(sound.getPitch(), 2.0952382f)
                    || scathaPro.config.dev.devModeEnabled.get() && sound.getPitch() >= 2f
            )
        ) {
            scathaPro.coreManager.lastPreAlertTime = now;
            ScathaProEvents.wormPreSpawnEvent.trigger(scathaPro);
        }

        // Mute non-Scatha-Pro sounds in Crystal Hollows
        if (!(sound instanceof ScathaProSound)
            && scathaPro.config.sounds.muteCrystalHollowsSounds.get())
        {
            boolean cancel = true;

            if (isVanillaSound(sound, "ui.button.click")) cancel = false;
            else if (scathaPro.config.sounds.keepDragonLairSounds.get()
                && (isVanillaSound(sound, "entity.ender_dragon.growl") || isVanillaSound(sound, "entity.ender_dragon.flap")))
                cancel = false;

            return !cancel;
        }

        return true;
    }

    private boolean isVanillaSound(@NonNull SoundInstance sound, @NonNull String path)
    {
        return sound.getIdentifier().equals(Identifier.withDefaultNamespace(path));
    }
}