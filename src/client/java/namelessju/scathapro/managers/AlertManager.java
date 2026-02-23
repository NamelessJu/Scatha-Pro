package namelessju.scathapro.managers;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.alerts.title.DynamicAlertTitleTemplate;
import namelessju.scathapro.alerts.title.FullAlertTitleTemplate;
import namelessju.scathapro.miscellaneous.IteratorWrapperImmutable;
import namelessju.scathapro.sounds.SoundData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AlertManager implements Iterable<Alert>
{
    public final Alert bedrockWallAlert;
    public final Alert oldLobbyAlert;
    
    public final Alert wormPreSpawnAlert;
    public final Alert regularWormSpawnAlert;
    public final Alert scathaSpawnAlert;
    public final Alert wormSpawnCooldownEndAlert;
    
    public final Alert scathaPetDropAlert;
    
    public final Alert highHeatAlert;
    public final Alert tunnelVisionReadyAlert;
    
    public final Alert goblinSpawnAlert;
    public final Alert jerrySpawnAlert;
    
    public final Alert antiSleepAlert;
    
    private final List<Alert> alerts = new ArrayList<>();
    
    public AlertManager(ScathaPro scathaPro)
    {
        bedrockWallAlert = register(new Alert(scathaPro, "bedrock_wall", "Bedrock Wall Alert", null,
            SoundData.vanilla("block.note_block.pling", 1f, 0.5f),
            new FullAlertTitleTemplate(
                null, "Close to bedrock",
                null, Style.EMPTY.withColor(ChatFormatting.GRAY),
                5, 20, 5
            ),
            scathaPro.config.alerts.bedrockWallAlertEnabled
        ));
        oldLobbyAlert = register(new Alert(scathaPro, "old_lobby", "Old Crystal Hollows Lobby Alert", null,
            SoundData.vanilla("block.note_block.pling", 1f, 0.5f),
            new FullAlertTitleTemplate(
                "Old Lobby", null,
                Style.EMPTY.withColor(ChatFormatting.RED), null,
                20, 40, 10
            ),
            scathaPro.config.alerts.oldLobbyAlertEnabled
        ));
        
        wormPreSpawnAlert = register(new Alert(scathaPro, "worm_prespawn", "Worm Pre-Spawn Alert",
            Component.literal("Triggers when either type of worm is about to spawn"),
            SoundData.vanilla("entity.experience_orb.pickup", 1f, 0.5f),
            new FullAlertTitleTemplate(
                null, "Worm About To Spawn...",
                null, Style.EMPTY.withColor(ChatFormatting.YELLOW),
                0, 20, 5
            ),
            scathaPro.config.alerts.wormPreSpawnAlertEnabled
        ));
        regularWormSpawnAlert = register(new Alert(scathaPro, "regular_worm_spawn", "Regular Worm Spawn Alert", null,
            SoundData.vanilla("entity.player.levelup", 1f, 0.5f),
            new FullAlertTitleTemplate(
                "Worm", "Just a regular worm...",
                Style.EMPTY.withColor(ChatFormatting.YELLOW), Style.EMPTY.withColor(ChatFormatting.GRAY),
                5, 20, 5
            ),
            scathaPro.config.alerts.regularWormSpawnAlertEnabled
        ));
        scathaSpawnAlert = register(new Alert(scathaPro, "scatha_spawn", "Scatha Spawn Alert", null,
            SoundData.vanilla("entity.player.levelup", 1f, 0.8f),
            new FullAlertTitleTemplate(
                "Scatha", "Pray to RNGesus!",
                Style.EMPTY.withColor(ChatFormatting.RED), Style.EMPTY.withColor(ChatFormatting.GRAY),
                0, 40, 10
            ),
            scathaPro.config.alerts.scathaSpawnAlertEnabled
        ));
        wormSpawnCooldownEndAlert = register(new Alert(scathaPro, "worm_spawn_cooldown_end", "Worm Spawn Cooldown Alert",
            Component.literal("Triggers when the worm spawn cooldown ends"),
            SoundData.vanilla("block.note_block.pling", 1f, 0.75f),
            new FullAlertTitleTemplate(
                null, "Worm Spawn Cooldown Ended",
                null, Style.EMPTY.withColor(ChatFormatting.GREEN),
                5, 30, 5
            ),
            scathaPro.config.alerts.wormSpawnCooldownEndAlertEnabled
        ));
        
        scathaPetDropAlert = register(new Alert(scathaPro, "scatha_pet_drop", "Scatha Pet Drop Alert", null,
            SoundData.vanilla("entity.wither.death", 0.75f, 0.8f),
            new DynamicAlertTitleTemplate(
                "Scatha Pet!", Style.EMPTY.withColor(ChatFormatting.YELLOW),
                0, 130, 20
            ),
            scathaPro.config.alerts.scathaPetDropAlertEnabled
        ));
        scathaPetDropAlert.setExtraSounds(
            SoundData.vanilla("block.chest.open", 1f, 0.95f)
        );
        
        highHeatAlert = register(new Alert(scathaPro, "high_heat", "High Heat Alert", null,
            SoundData.vanilla("item.firecharge.use", 1f, 1f),
            new FullAlertTitleTemplate(
                "High Heat", "Cool down a little!",
                Style.EMPTY.withColor(ChatFormatting.RED), Style.EMPTY.withColor(ChatFormatting.AQUA),
                5, 40, 5
            ),
            scathaPro.config.alerts.highHeatAlertEnabled
        ));
        /* Note: do NOT change ID as custom mode files depend on it! */
        tunnelVisionReadyAlert = register(new Alert(scathaPro, "anomalous_desire_ready", "Tunnel Vision Ready Alert",
            Component.literal("Triggers when you should use the Tunnel Vision pickaxe ability again"),
            SoundData.vanilla("entity.experience_orb.pickup", 1f, 1f),
            new FullAlertTitleTemplate(
                "Tunnel Vision Ready", null,
                Style.EMPTY.withColor(ChatFormatting.GREEN), null,
                5, 20, 5
            ),
            scathaPro.config.alerts.pickaxeAbilityReadyAlertEnabled
        ));
        
        goblinSpawnAlert = register(new Alert(scathaPro, "goblin_spawn", "Goblin Spawn Alert",
            Component.literal("Triggers when a golden or diamond goblin spawns"),
            SoundData.vanilla("entity.player.levelup", 1f, 1.25f),
            new DynamicAlertTitleTemplate(
                "Goblin", Style.EMPTY.withColor(ChatFormatting.DARK_GREEN),
                3, 30, 5
            ),
            scathaPro.config.alerts.goblinSpawnAlertEnabled
        ));
        jerrySpawnAlert = register(new Alert(scathaPro, "jerry_spawn", "Jerry Spawn Alert",
            Component.literal("Triggers when a hidden Jerry (mayor perk) spawns"),
            SoundData.vanilla("entity.player.levelup", 1f, 1.5f),
            new DynamicAlertTitleTemplate(
                "Jerry", Style.EMPTY.withColor(ChatFormatting.AQUA),
                5, 40, 10
            ),
            scathaPro.config.alerts.jerrySpawnAlertEnabled
        ));
        
        antiSleepAlert = register(new Alert(scathaPro, "anti_sleep", "Anti-Sleep Alert",
            Component.literal("Plays a loud sound in a random interval to keep you awake"),
            SoundData.scathaPro("alert.anti_sleep", 1f, 1f),
            new FullAlertTitleTemplate(
                null, null,
                null, Style.EMPTY.withColor(ChatFormatting.GRAY),
                0, 40, 20
            ),
            scathaPro.config.alerts.antiSleepAlertEnabled
        ));
    }
    
    private Alert register(Alert alert)
    {
        this.alerts.add(alert);
        return alert;
    }
    
    @Override
    public @NonNull Iterator<Alert> iterator()
    {
        return new IteratorWrapperImmutable<>(alerts.iterator());
    }
}
