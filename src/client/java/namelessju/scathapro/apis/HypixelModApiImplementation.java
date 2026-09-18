package namelessju.scathapro.apis;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.data.enums.HypixelEnvironment;
import namelessju.scathapro.miscellaneous.data.enums.SkyBlockArea;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class HypixelModApiImplementation
{
    private HypixelModApiImplementation() {}

    public static void init(ScathaPro scathaPro)
    {
        HypixelModAPI modApi = HypixelModAPI.getInstance();

        modApi.createHandler(ClientboundHelloPacket.class, packet -> {
            ScathaPro.LOGGER.debug("Received Hypixel Hello packet for environment {}", packet.getEnvironment().name());
            scathaPro.hypixelContextManager.setEnvironment(switch (packet.getEnvironment())
            {
                case PRODUCTION -> HypixelEnvironment.PRODUCTION;
                case BETA -> HypixelEnvironment.ALPHA;
                case null, default -> HypixelEnvironment.OTHER;
            });
        });

        modApi.subscribeToEventPacket(ClientboundLocationPacket.class);
        modApi.createHandler(ClientboundLocationPacket.class, packet -> {
            AtomicReference<SkyBlockArea> newArea = new AtomicReference<>();
            AtomicBoolean isSkyBlock = new AtomicBoolean(false);
            packet.getServerType().ifPresent(serverType -> {
                if (serverType.name().equals("SKYBLOCK")) isSkyBlock.set(true);
            });
            if (isSkyBlock.get())
            {
                ScathaPro.LOGGER.debug("Server is of type SKYBLOCK!");

                packet.getMode().ifPresent(mode -> {
                    for (SkyBlockArea area : SkyBlockArea.values())
                    {
                        if (area.serverModeId.equals(mode))
                        {
                            newArea.set(area);
                            break;
                        }
                    }

                    if (newArea.get() == null) ScathaPro.LOGGER.debug("Encountered unknown mode while detecting Skyblock area: {}", mode);
                });
            }

            scathaPro.hypixelContextManager.setLocation(isSkyBlock.get(), newArea.get());
        });

        ScathaPro.LOGGER.debug("Hypixel mod API initialized");
    }
}