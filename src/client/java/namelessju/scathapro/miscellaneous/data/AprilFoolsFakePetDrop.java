package namelessju.scathapro.miscellaneous.data;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.data.enums.Rarity;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class AprilFoolsFakePetDrop extends ScathaPetDrop
{
    public static void play(ScathaPro scathaPro)
    {
        AprilFoolsFakePetDrop petDrop = new AprilFoolsFakePetDrop();
        Component dropMessage = Constants.generatePetDropMessage(Rarity.RARE);
        if (scathaPro.scathaDropsGamblingReelManager.isRolling())
        {
            scathaPro.scathaDropsGamblingReelManager.setPetDrop(petDrop, false);
            scathaPro.scathaDropsGamblingReelManager.addDelayedChatMessage(dropMessage);
        }
        else
        {
            scathaPro.chatManager.sendChatMessage(dropMessage, false);
            petDrop.trigger(scathaPro);
        }
    }

    private AprilFoolsFakePetDrop()
    {
        super(Rarity.RARE, false, true);
    }

    @Override
    public void trigger(ScathaPro scathaPro)
    {
        super.trigger(scathaPro);
        scathaPro.coreManager.aprilFoolsJokeRevealTickTimer = 40;
    }
}