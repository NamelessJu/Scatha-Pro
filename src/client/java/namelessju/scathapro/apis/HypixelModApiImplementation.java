package namelessju.scathapro.apis;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.miscellaneous.data.enums.SkyBlockArea;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class HypixelModApiImplementation
{
    private HypixelModApiImplementation() {}

    public static void init(ScathaPro scathaPro)
    {
        HypixelModAPI modApi = HypixelModAPI.getInstance();

        modApi.subscribeToEventPacket(ClientboundLocationPacket.class);
        modApi.createHandler(ClientboundLocationPacket.class, packet -> {
            AtomicReference<SkyBlockArea> newArea = new AtomicReference<>();
            AtomicBoolean isSkyblock = new AtomicBoolean(false);
            packet.getServerType().ifPresent(serverType -> {
                if (serverType.name().equals("SKYBLOCK")) isSkyblock.set(true);
            });
            if (isSkyblock.get())
            {
                ScathaPro.LOGGER.debug("Server is of type SKYBLOCK!");

                packet.getMode().ifPresent(mode -> {
                    for (SkyBlockArea area : SkyBlockArea.values())
                    {
                        if (area.serverModeId.equals(mode))
                        {
                            newArea.set(area);
                            ScathaProEvents.skyBlockAreaDetectedEvent.trigger(
                                new ScathaProEvents.SkyBlockAreaDetectedEventData(scathaPro, area)
                            );
                            ScathaPro.LOGGER.debug("Skyblock area detected: {}", area.name());
                            break;
                        }
                    }

                    if (newArea.get() == null) ScathaPro.LOGGER.debug("Encountered unknown mode while detecting Skyblock area: {}", mode);
                });
            }

            scathaPro.coreManager.setSkyBlockArea(newArea.get());
        });

        ScathaPro.LOGGER.debug("Hypixel mod API initialized");
    }
}