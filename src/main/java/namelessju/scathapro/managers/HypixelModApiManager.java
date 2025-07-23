package namelessju.scathapro.managers;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.SkyblockAreaDetectedEvent;
import namelessju.scathapro.miscellaneous.enums.SkyblockArea;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import net.minecraftforge.common.MinecraftForge;

public class HypixelModApiManager
{
    private HypixelModApiManager() {}
    
    public static void init(ScathaPro scathaPro)
    {
        HypixelModAPI modApi = HypixelModAPI.getInstance();
        
        modApi.subscribeToEventPacket(ClientboundLocationPacket.class);
        modApi.createHandler(ClientboundLocationPacket.class, packet -> {
            
            if (scathaPro.getConfig().getBoolean(Config.Key.devMode)) return;
            
            scathaPro.variables.currentArea = null;
            
            String mode = packet.getMode().orElse(null);
            if (mode == null) return;
            
            for (SkyblockArea area : SkyblockArea.values())
            {
                if (area.serverModeId.equals(mode))
                {
                    scathaPro.variables.currentArea = area;
                    scathaPro.logDebug("Area detected: " + area.name());
                    MinecraftForge.EVENT_BUS.post(new SkyblockAreaDetectedEvent(area));
                    return;
                }
            }
            
            scathaPro.logDebug("Encountered unknown mode while detecting area: " + mode);
        });
        
        scathaPro.logDebug("Hypixel mod API initialized");
    }
}
