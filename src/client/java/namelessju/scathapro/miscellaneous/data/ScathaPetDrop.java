package namelessju.scathapro.miscellaneous.data;

import it.unimi.dsi.fastutil.ints.IntList;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.miscellaneous.data.enums.Rarity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class ScathaPetDrop
{
    private final Rarity rarity;

    public boolean isEffectsOnly = false;

    public ScathaPetDrop(Rarity rarity)
    {
        this.rarity = rarity;
    }

    public Rarity rarity()
    {
        return rarity;
    }

    public void trigger(ScathaPro scathaPro)
    {
        if (!isEffectsOnly)
        {
            ScathaProEvents.scathaPetDropEvent.trigger(
                new ScathaProEvents.ScathaPetDropEventData(scathaPro, this)
            );
        }

        playEffects(scathaPro);
    }

    private void playEffects(ScathaPro scathaPro)
    {
        scathaPro.alertManager.scathaPetDropAlert.play(scathaPro,
            Component.literal(rarity.rarityName.toUpperCase()).setStyle(rarity.style)
        );

        if (scathaPro.config.petDrop.itemPopupEnabled.get())
        {
            scathaPro.itemPopupRenderer.popup(
                Constants.generateScathaPetItem(rarity),
                Mth.clamp(scathaPro.config.petDrop.itemPopupAnimationTicks.get(), 1, 200),
                scathaPro.config.petDrop.itemPopupUseAltRotAnimCurve.get(),
                true
            );
        }

        if (scathaPro.config.petDrop.fireworkEnabled.get())
        {
            LocalPlayer player = scathaPro.minecraft.player;
            if (player != null)
            {
                Vec3 particlePos = player.getEyePosition().add(player.getForward().scale(1f));
                player.level().createFireworks(
                    particlePos.x, particlePos.y, particlePos.z, 0f, 0f, 0f,
                    List.of(new FireworkExplosion(
                        FireworkExplosion.Shape.SMALL_BALL,
                        IntList.of(rarity.color), IntList.of(),
                        false, true
                    ))
                );
            }
        }
    }
}