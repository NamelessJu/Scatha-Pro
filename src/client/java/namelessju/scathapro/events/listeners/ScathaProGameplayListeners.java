package namelessju.scathapro.events.listeners;

import com.google.gson.JsonObject;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.miscellaneous.data.AprilFoolsFakePetDrop;
import namelessju.scathapro.parsing.PlayerListParser;
import namelessju.scathapro.util.*;
import net.minecraft.client.Screenshot;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;

import java.math.RoundingMode;

public final class ScathaProGameplayListeners
{
    private ScathaProGameplayListeners() {}

    public static void register()
    {
        ScathaProEvents.wormPreSpawnEvent.addListener(ScathaProGameplayListeners::onWormPreSpawn);
        ScathaProEvents.wormSpawnEvent.addListener(ScathaProGameplayListeners::onWormSpawn);
        ScathaProEvents.wormHitEvent.addListener(ScathaProGameplayListeners::onWormHit);
        ScathaProEvents.wormKillEvent.addListener(ScathaProGameplayListeners::onWormKill);
        ScathaProEvents.wormDespawnEvent.addListener(ScathaProGameplayListeners::onWormDespawn);
        ScathaProEvents.scathaPetDropEvent.addListener(ScathaProGameplayListeners::onScathaPetDrop);
        ScathaProEvents.scathaExtraItemDropEvent.addListener(ScathaProGameplayListeners::onScathaExtraItemDrop);
        ScathaProEvents.bedrockWallDetectedEvent.addListener(ScathaProGameplayListeners::onBedrockWall);
    }

    private static void onWormPreSpawn(ScathaPro scathaPro)
    {
        scathaPro.coreManager.startWormSpawnCooldown(true);
        scathaPro.alertManager.wormPreSpawnAlert.play(scathaPro);
        scathaPro.mainOverlay.setShown(true);
        scathaPro.achievementLogicManager.handleTunnelVisionRecoverAchievement();
    }

    private static void onWormSpawn(ScathaProEvents.WormEventData data)
    {
        ScathaPro scathaPro = data.scathaPro();

        scathaPro.mainOverlay.setShown(true);

        LocalPlayer player = scathaPro.minecraft.player;
        if (player == null) return;

        boolean spawnedBySelf = false;

        AABB playerDetectionAABB = AABB.ofSize(player.position(), 70, 14, 70);
        int nearbyPlayerCount = player.level().getEntities(player, playerDetectionAABB, entity -> entity instanceof Player).size();

        BlockPos playerPos = player.blockPosition();
        BlockPos wormPos = data.worm().entity.blockPosition().below(); // down because the armor stand is floating 1 block above ground

        if (nearbyPlayerCount == 0) spawnedBySelf = true;
        else
        {
            int xDistance = Math.abs(playerPos.getX() - wormPos.getX());
            int zDistance = Math.abs(playerPos.getZ() - wormPos.getZ());

            if (
                xDistance <= 1 || zDistance <= 1
                && Math.abs(playerPos.getY() - wormPos.getY()) <= 1
            ) {
                spawnedBySelf = true;

                if (xDistance > 1 || zDistance > 1)
                {
                    int[] obstructionCheckDirection = {0, 0};

                    BlockPos currentCheckPos = wormPos;
                    BlockPos targetPos;

                    if (xDistance >= zDistance)
                    {
                        obstructionCheckDirection[0] = (int) Math.signum(playerPos.getX() - wormPos.getX());
                        targetPos = new BlockPos(playerPos.getX(), wormPos.getY(), wormPos.getZ());
                    }
                    else
                    {
                        obstructionCheckDirection[1] = (int) Math.signum(playerPos.getZ() - wormPos.getZ());
                        targetPos = new BlockPos(wormPos.getX(), wormPos.getY(), playerPos.getZ());
                    }

                    for (int i = 0; i < 5; i ++)
                    {
                        currentCheckPos = currentCheckPos.offset(obstructionCheckDirection[0], 0, obstructionCheckDirection[1]);

                        if (currentCheckPos.equals(targetPos)) break;

                        Block block = player.level().getBlockState(currentCheckPos).getBlock();
                        if (block != Blocks.AIR && block != Blocks.LAVA && block != Blocks.WATER)
                        {
                            ScathaPro.LOGGER.debug("Worm ({}) not in FOV of player ({}) - blocked by {} at {}", wormPos, playerPos, block, currentCheckPos);
                            spawnedBySelf = false;
                            break;
                        }
                    }
                }
            }
            else
            {
                ScathaPro.LOGGER.debug("Worm spawn detection: player wasn't close to the worm's X or Z position");
            }
        }

        if (spawnedBySelf) ScathaPro.LOGGER.debug("Worm spawned: assumed to be spawned by this client's player");
        else ScathaPro.LOGGER.debug("Worm spawned: assumed to be spawned by other player");

        if (!spawnedBySelf) return;

        // Scatha spawn
        if (data.worm().isScatha)
        {
            scathaPro.alertManager.scathaSpawnAlert.play(scathaPro);
            scathaPro.secondaryStatsManager.addScathaSpawn();

            scathaPro.achievementLogicManager.updateScathaSpawnAchievements(data.worm());

            scathaPro.scathaDropsSlotMachineManager.reset();
        }
        // Regular worm spawn
        else
        {
            scathaPro.alertManager.regularWormSpawnAlert.play(scathaPro);
            scathaPro.secondaryStatsManager.addRegularWormSpawn();
        }

        long now = TimeUtil.getEpochMilliseconds();

        long timeSincePreviousSpawn = scathaPro.coreManager.lastWormSpawnTime >= 0L ? now - scathaPro.coreManager.lastWormSpawnTime : -1L;
        if (timeSincePreviousSpawn >= 0L)
        {
            if (scathaPro.config.miscellaneous.wormSpawnTimerMessageEnabled.get())
            {
                int secondsSinceLastSpawn = (int) Math.floor(timeSincePreviousSpawn / 1000D);
                String timeComponent;
                if (secondsSinceLastSpawn < 60) timeComponent = secondsSinceLastSpawn + " seconds";
                else timeComponent = TextUtil.numberToString(secondsSinceLastSpawn / 60D, 1, true, RoundingMode.FLOOR)
                                     + " minutes";
                scathaPro.chatManager.sendChatMessage(
                    Component.literal("Worm spawned " + timeComponent + " after previous worm").withColor(TextColor.GRAY)
                );
            }
        }

        scathaPro.persistentData.save();

        scathaPro.coreManager.lastWormSpawnTime = now;
        scathaPro.coreManager.startWormSpawnCooldown(false); // in case pre-spawn doesn't trigger when master volume is 0
        scathaPro.mainOverlay.updateWormStreak();

        scathaPro.achievementLogicManager.updateSpawnAchievements();
    }

    private static void onWormHit(ScathaProEvents.WormHitEventData data)
    {
        ScathaPro scathaPro = data.scathaPro();
        if (data.worm().isScatha)
        {
            Component customName = data.worm().entity.getCustomName();
            if (customName != null)
            {
                String unformattedName = customName.getString();

                scathaPro.coreManager.lastScathaHitHadShuriken = (
                    unformattedName.indexOf(UnicodeSymbol.shurikenTag) >= 0
                    || unformattedName.indexOf(UnicodeSymbol.magicFind) >= 0 // in case Hypixel fixes their spaghetti code
                );

                if (scathaPro.config.miscellaneous.dropsSlotMachineEnabled.get()
                    && unformattedName.contains("1" + UnicodeSymbol.hypixelHeart))
                {
                    scathaPro.scathaDropsSlotMachineManager.startPreRoll();
                }
            }
            else scathaPro.coreManager.lastScathaHitHadShuriken = false;

            String skyBlockItemID = SkyBlockItemUtil.getItemID(data.weapon());
            switch (skyBlockItemID)
            {
                case Constants.ItemID.dirt -> Achievement.scatha_hit_dirt.unlock();
                case Constants.ItemID.fineTopaz, Constants.ItemID.fineAmethyst, Constants.ItemID.fineJade,
                     Constants.ItemID.fineAmber, Constants.ItemID.fineSapphire -> Achievement.scatha_kill_gemstone.unlock();
                case Constants.ItemID.pet -> {
                    CompoundTag itemData = SkyBlockItemUtil.getData(data.weapon());
                    if (itemData == null) break;
                    itemData.getString(SkyBlockItemUtil.KEY_PET_INFO).ifPresent(petInfo -> {
                        if (!(JsonUtil.parseJson(petInfo) instanceof JsonObject petInfoParsed)) return;
                        String petType = JsonUtil.getString(petInfoParsed, "type");
                        if (petType == null || !petType.equals("SCATHA")) return;
                        Achievement.scatha_kill_gemstone.unlock();
                    });
                }
                case null, default -> {}
            }

            ScathaPro.LOGGER.debug("Scatha hit - has shuriken: {}", scathaPro.coreManager.lastScathaHitHadShuriken);
        }
    }

    private static void onWormKill(ScathaProEvents.WormKillEventData data)
    {
        ScathaPro scathaPro = data.scathaPro();

        scathaPro.mainOverlay.setShown(true);
        PlayerListParser.parseProfileStats(scathaPro);

        if (data.worm().isScatha)
        {
            scathaPro.coreManager.addScathaKill();
            scathaPro.coreManager.lastScathaKillTime = TimeUtil.getEpochMilliseconds();

            if (scathaPro.config.miscellaneous.dropsSlotMachineEnabled.get())
            {
                scathaPro.scathaDropsSlotMachineManager.startRolling();
            }

            if (TimeUtil.isAprilFools() && TimeUtil.getCurrentYear() != scathaPro.getProfileData().lastAprilFoolsJokeShownYear.getOr(-1)
                && scathaPro.config.miscellaneous.aprilFoolsFakeDropEnabled.get())
            {
                AprilFoolsFakePetDrop.play(scathaPro);
            }

            if (data.worm().getHitWeaponsCount() > 0)
            {
                if (data.worm().getHitWeaponsCount() >= Achievement.kill_weapons_scatha.goal)
                {
                    Achievement.kill_weapons_scatha.unlock();
                }

                String lastHitWeapon = data.worm().getHitWeapons()[data.worm().getHitWeaponsCount() - 1];
                switch (lastHitWeapon)
                {
                    case Constants.ItemID.terminator -> Achievement.scatha_kill_terminator.unlock();
                    case Constants.ItemID.jujuShortbow -> Achievement.scatha_kill_juju.unlock();
                }

                if (data.worm().wasHitWithPerfectGemstoneGauntlet()) Achievement.kill_perfect_gemstone_gauntlet.unlock();
            }

            if (data.wasBlackHoled())
            {
                Achievement.scatha_kill_black_hole.unlock();

                Vec2 blackHolePos = data.worm().getBlackHolePosition();
                if (blackHolePos == null)
                {
                    Entity nearbyBlackHole = Constants.getNearbyBlackHole(data.worm().entity);
                    if (nearbyBlackHole != null) blackHolePos = Util.getHorizontal(nearbyBlackHole.position());
                }
                if (blackHolePos != null)
                {
                    if (scathaPro.coreManager.lastScathaBlackHolePosition != null
                        && blackHolePos.distanceToSqr(scathaPro.coreManager.lastScathaBlackHolePosition) < 0.01f) // = 0.1 because sqrd
                    {
                        Achievement.scatha_kill_black_hole_2_in_1.unlock();
                    }
                    scathaPro.coreManager.lastScathaBlackHolePosition = blackHolePos;
                }
            }
            else if (scathaPro.coreManager.lastScathaHitHadShuriken)
            {
                Achievement.scatha_kill_shuriken.unlock();
            }

            LocalPlayer player = scathaPro.minecraft.player;
            if (player != null)
            {
                if (player.isCrouching() && scathaPro.coreManager.lastCrouchStartTime >= 0
                    && data.worm().spawnTime >= scathaPro.coreManager.lastCrouchStartTime)
                {
                    Achievement.scatha_kill_sneak.unlock();
                }
                // note: this armor stand (= Scatha name tag) is positioned a little bit above the worm visuals
                if (player.getY() - data.worm().entity.getY() >= 0)
                {
                    Achievement.scatha_kill_highground.unlock();
                }
            }

            // seems weird but works because it would only be a problem on a kill that is about to trigger the b2b drop
            // but in that case it happens so fast that it won't ever be noticed
            Achievement.scatha_pet_drop_b2b.setProgress(0f);

            scathaPro.mainOverlay.updateScathaKills();
            scathaPro.mainOverlay.updateScathaKillsSinceLastDrop();
            scathaPro.coreManager.updateScathaFarmingStreak(true);

            if (!scathaPro.getProfileData().scappaModeUnlocked.get())
            {
                if (Util.random.nextFloat() < 0.004f)
                {
                    scathaPro.coreManager.scappaModeActiveTemp = true;
                    scathaPro.getProfileData().scappaModeUnlocked.set(true);

                    scathaPro.chatManager.sendChatDivider();
                    scathaPro.chatManager.sendChatMessage(Component.empty()
                        .append(TextUtil.getRainbowText("Scappa mode unlocked!"))
                        .append("\n")
                        .append(Component.literal(
                                "This was a 1/250 chance per Scatha kill."
                                + " It will stay active for this game session, afterwards you can from now on toggle it freely under "
                                + scathaPro.getModDisplayName() + " Settings > Miscellaneous."
                            ).withColor(TextColor.GRAY)
                        )
                    );
                    scathaPro.chatManager.sendChatDivider();

                    Achievement.scappa_mode.unlock();

                    scathaPro.mainOverlay.updateScappaMode();
                }
            }
        }
        else
        {
            scathaPro.coreManager.addRegularWormKill();
            scathaPro.mainOverlay.updateWormKills();
        }

        scathaPro.persistentData.save();

        scathaPro.achievementLogicManager.updateKillsAchievements();
        if (data.worm().getCurrentLifetime() <= 1000) Achievement.worm_kill_time_1.unlock();
        else if (data.worm().getCurrentLifetime() >= Constants.wormLifetime - 5000) Achievement.worm_kill_time_2.unlock();
    }

    private static void onWormDespawn(ScathaProEvents.WormEventData data)
    {
        Achievement.worm_despawn.unlock();
    }

    private static void onScathaPetDrop(ScathaProEvents.ScathaPetDropEventData data)
    {
        ScathaPro scathaPro = data.scathaPro();
        PersistentData.ProfileData profileData = scathaPro.getProfileData();
        int scathaKillsAtLastDropBefore = profileData.scathaKillsAtLastDrop.getOr(-1);


        switch (data.scathaPetDrop().rarity())
        {
            case RARE:
                profileData.rarePetDrops.set(profileData.rarePetDrops.get() + 1);
                break;

            case EPIC:
                profileData.epicPetDrops.set(profileData.epicPetDrops.get() + 1);
                break;

            case LEGENDARY:
                profileData.legendaryPetDrops.set(profileData.legendaryPetDrops.get() + 1);
                break;
        }

        scathaPro.achievementLogicManager.updatePetDropAchievements();

        switch (scathaPro.config.alerts.mode.get().id)
        {
            case "normal":
                Achievement.scatha_pet_drop_mode_normal.unlock();
                break;
            case "meme":
                Achievement.scatha_pet_drop_mode_meme.unlock();
                break;
            case "anime":
                Achievement.scatha_pet_drop_mode_anime.unlock();
                break;
            case "custom":
                if (scathaPro.customAlertModeManager.getCurrentSubModeId() != null)
                {
                    Achievement.scatha_pet_drop_mode_custom.unlock();
                }
                break;
        }

        if (scathaKillsAtLastDropBefore >= 0 && profileData.scathaKills.get() >= 0
            && profileData.scathaKills.get() == scathaKillsAtLastDropBefore + 1)
        {
            Achievement.scatha_pet_drop_b2b.unlock();
        }
        Achievement.scatha_pet_drop_b2b.setProgress(1f);

        if (profileData.scathaKills.get() >= 0)
        {
            profileData.scathaKillsAtLastDrop.set(profileData.scathaKills.get());
            scathaPro.mainOverlay.updateScathaKillsSinceLastDrop();

            scathaPro.achievementLogicManager.updateDryStreakAchievements();
        }

        profileData.isPetDropDryStreakInvalidated.set(false);

        scathaPro.persistentData.save();


        if (scathaPro.config.miscellaneous.dryStreakMessageEnabled.get()
            && profileData.scathaKills.get() >= 0)
        {
            boolean droppedPetBefore = scathaKillsAtLastDropBefore >= 0;
            int dryStreak = profileData.scathaKills.get() - 1;
            if (droppedPetBefore) dryStreak -= scathaKillsAtLastDropBefore;

            if (!profileData.isPetDropDryStreakInvalidated.get())
            {
                if (dryStreak > 0)
                {
                    scathaPro.chatManager.sendChatMessage(Component.empty().withColor(TextColor.YELLOW)
                        .append("Scatha pet dropped after a ")
                        .append(Component.literal(dryStreak + " Scatha kill" + (dryStreak != 1 ? "s" : "")).withColor(TextColor.RED))
                        .append(" dry streak"));
                }
                else if (dryStreak == 0)
                {
                    if (droppedPetBefore)
                    {
                        scathaPro.chatManager.sendChatMessage(Component.empty().withColor(TextColor.YELLOW)
                            .append(Component.literal("BACK TO BACK").withColor(TextColor.RED))
                            .append(" Scatha pet drop!"));
                    }
                    else
                    {
                        scathaPro.chatManager.sendChatMessage(Component.empty().withColor(TextColor.YELLOW)
                            .append("Scatha pet drop ")
                            .append(Component.literal("ON FIRST SCATHA KILL").withColor(TextColor.RED))
                            .append("!"));
                    }
                }
            }
            else if (!droppedPetBefore)
            {
                scathaPro.chatManager.sendChatMessage(Component.empty().setStyle(Style.EMPTY
                        .withColor(TextColor.YELLOW)
                        .withHoverEvent(new HoverEvent.ShowText(Component.empty().withColor(TextColor.GRAY)
                            .append(Component.literal("Note:\n").withColor(TextColor.YELLOW))
                            .append(scathaPro.getModDisplayName() + " isn't able to determine whether you\n"
                                + "dropped a pet before since you already killed\n"
                                + "several Scathas before installing the mod")))
                    )
                    .append("Scatha pet dropped after ")
                    .append(Component.literal(dryStreak + " Scatha kill" + (dryStreak != 1 ? "s" : "")).withColor(TextColor.RED))
                    .append(", this may or may not be your dry streak!"));
            }
            else
            {
                scathaPro.chatManager.sendChatMessage(Component.empty().setStyle(Style.EMPTY
                        .withColor(TextColor.GRAY)
                        .withHoverEvent(new HoverEvent.ShowText(Component.empty().withColor(TextColor.GRAY)
                            .append(Component.literal("Note:\n").withColor(TextColor.YELLOW))
                            .append("""
                                You may have dropped another pet during
                                this kill count since the registered number
                                differed too much from the bestiary amount""")
                        ))
                    )
                    .append("Dry streak: " + dryStreak + " (may be incorrect)"));
            }


            if (scathaPro.config.miscellaneous.automaticPetDropScreenshotEnabled.get())
            {
                scathaPro.runNextTick(() -> Screenshot.grab(scathaPro.minecraft.gameDirectory,
                    ScathaPro.MOD_NAME + "_Pet_Drop_" + net.minecraft.util.Util.getFilenameFormattedDateTime() + ".png",
                    scathaPro.minecraft.gameRenderer.mainRenderTarget(), 1,
                    scathaPro.chatManager::sendChatMessage
                ));
            }
        }
    }

    private static void onScathaExtraItemDrop(ScathaProEvents.ScathaExtraDropEventData data)
    {
        ScathaPro scathaPro = data.scathaPro();

        if (data.itemType() == ScathaProEvents.ScathaExtraDropEventData.ItemType.DWARVEN_OS_BLOCK_BRAN)
        {
            scathaPro.getProfileData().blockBransDropped.set(scathaPro.getProfileData().blockBransDropped.get() + 1);
            scathaPro.secondaryStatsManager.addBlockBran();
            scathaPro.persistentData.save();
            scathaPro.scathaDropsSlotMachineManager.setHasDroppedBlockBran(true);
        }
    }

    private static void onBedrockWall(ScathaPro scathaPro)
    {
        scathaPro.alertManager.bedrockWallAlert.play(scathaPro);
    }
}