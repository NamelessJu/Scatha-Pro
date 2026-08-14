package namelessju.scathapro.gui.menus.screens.settings;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.files.Config;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.gui.menus.widgets.ItemWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class WormSettingsScreen extends ConfigScreen
{
    public WormSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Worm Settings", parentScreen);
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();


        GridBuilder gridBuilder = new GridBuilder();

        gridBuilder.addFullWidth(booleanConfigButton(
            "Show Lifetime Left", config.worms.showLifetimeLeft,
            _ -> Tooltip.create(
                Component.literal("Displays the amount of seconds left before a worm will despawn above it's nametag")
                    .withColor(TextColor.GRAY)
            ), null
        ));

        gridBuilder.addGap();

        gridBuilder.addFullWidth(booleanConfigButton("Revert Worm Texture", config.worms.revertRegularWormTexture,
            _ -> Tooltip.create(
                Component.literal("Replaces the new Stoneworm texture with the old one (= the same as Scathas)")
                    .withColor(TextColor.GRAY)
            ), null
        ));
        gridBuilder.addMultipliedGap(0.5f);
        gridBuilder.addSingleCell(
            new StringWidget(0, minecraft.font.lineHeight, Component.literal("Stoneworm Player Head:"), font),
            true, false
        );
        gridBuilder.addSingleCell(
            new StringWidget(0, minecraft.font.lineHeight, Component.literal("Scatha Player Head:"), font),
            true, false
        );
        gridBuilder.addAbsoluteGap(0);
        gridBuilder.addSingleCell(new PlayerHeadProfileEditSection(minecraft,
            config.worms.regularWormPlayerHeadProfile, Constants.wormHeadTextureDefault
        ).setStoneworm());
        gridBuilder.addSingleCell(new PlayerHeadProfileEditSection(minecraft,
            config.worms.scathaPlayerHeadProfile, Constants.scathaHeadTextureDefault
        ));

        gridBuilder.addToContent(layout);


        addDoneButtonFooter();
    }

    private static final class PlayerHeadProfileEditSection implements Layout
    {
        private final Config.@NonNull ResolvableProfileValue configValue;
        private final @Nullable ItemStack itemStack;
        private final ResolvableProfile defaultProfile;

        private final LinearLayout rootElement;
        private final @Nullable ItemWidget itemWidget;
        private final EditBox playerHeadNameEditBox;
        private final StringWidget playerHeadNameLabel;
        private final Button editPlayerHeadButton;

        private boolean isEditingPlayerHead = false;
        private CompletableFuture<GameProfile> resolvingProfile = null;

        public PlayerHeadProfileEditSection(@NonNull Minecraft minecraft, Config.@NonNull ResolvableProfileValue configValue, @NonNull String defaultTextureBase64)
        {
            this.configValue = configValue;

            if (BuiltInRegistries.ITEM.wrapAsHolder(Items.PLAYER_HEAD).areComponentsBound())
                itemStack = new ItemStack(Items.PLAYER_HEAD);
            else itemStack = null;

            this.defaultProfile = ResolvableProfile.createResolved(new GameProfile(
                new UUID(0L, 0L), "",
                new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", defaultTextureBase64)))
            ));


            rootElement = new LinearLayout(0, 0, LinearLayout.Orientation.HORIZONTAL);
            rootElement.spacing(4);
            rootElement.defaultCellSetting().alignVerticallyMiddle().alignHorizontallyLeft();

            Component playerNameComponent = Component.literal("Player Name");
            playerHeadNameEditBox = new EditBox(minecraft.font, 0, 0, playerNameComponent);
            playerHeadNameEditBox.setHint(playerNameComponent);
            playerHeadNameEditBox.setMaxLength(16);

            if (itemStack != null)
            {
                itemWidget = rootElement.addChild(new ItemWidget(0, 0, 16, 16, itemStack));
            }
            else itemWidget = null;

            FrameLayout playerNameLayout = new FrameLayout(0, 0);
            playerNameLayout.defaultChildLayoutSetting().alignHorizontallyLeft().alignVerticallyMiddle();
            playerNameLayout.addChild(playerHeadNameEditBox);
            playerNameLayout.addChild(playerHeadNameLabel = new StringWidget(0, minecraft.font.lineHeight - 1, Component.empty(), minecraft.font));
            rootElement.addChild(playerNameLayout);

            rootElement.addChild(editPlayerHeadButton = Button.builder(Component.empty(), _ -> {
                isEditingPlayerHead = !isEditingPlayerHead;
                if (!isEditingPlayerHead)
                {
                    if (resolvingProfile != null) resolvingProfile.cancel(true);
                    String playerName = playerHeadNameEditBox.getValue().trim();
                    if (playerName.isBlank()) setProfile(null);
                    else
                    {
                        resolvingProfile = ResolvableProfile.createUnresolved(playerName)
                            .resolveProfile(minecraft.services().profileResolver());
                        resolvingProfile.whenComplete((gameProfile, throwable) -> {
                            if (throwable != null)
                            {
                                setProfile(null);
                                ScathaPro.LOGGER.error("Failed to load worm player head profile", throwable);
                            }
                            else setProfile(gameProfile);
                            updatePlayerHead();
                        });
                    }
                }
                updatePlayerHead();
            }).size(50, 0).build());


            updateLayout();
            updatePlayerHead();
        }

        public PlayerHeadProfileEditSection setStoneworm()
        {
            if (itemStack != null) itemStack.set(DataComponents.CUSTOM_NAME, Component.literal("Stoneworm"));
            return this;
        }

        public void setProfile(GameProfile profile)
        {
            configValue.setResolved(profile);
        }

        private void updatePlayerHead()
        {
            GameProfile profile = configValue.getResolved();

            if (isEditingPlayerHead)
            {
                playerHeadNameEditBox.setEditable(true);
                playerHeadNameEditBox.visible = true;
                playerHeadNameEditBox.setValue(profile != null ? profile.name() : "");
                playerHeadNameLabel.visible = false;
                editPlayerHeadButton.setMessage(Component.literal("Save"));
            }
            else
            {
                playerHeadNameEditBox.setEditable(false);
                playerHeadNameEditBox.visible = false;
                if (resolvingProfile != null && !resolvingProfile.isDone())
                {
                    playerHeadNameLabel.setMessage(Component.literal("Loading...").setStyle(Style.EMPTY.withColor(TextColor.GRAY).withItalic(true)));
                }
                else
                {
                    if (resolvingProfile != null && resolvingProfile.isCompletedExceptionally())
                    {
                        playerHeadNameLabel.setMessage(Component.literal("Failed to load profile").withColor(TextColor.RED));
                    }
                    else if (profile != null)
                    {
                        if (!profile.name().isBlank())
                        {
                            MutableComponent component = Component.empty();
                            if (itemStack == null)
                                component
                                    .append(Component.object(new PlayerSprite(ResolvableProfile.createResolved(profile), true)))
                                    .append(" ");
                            component.append(Component.literal(profile.name()));
                            playerHeadNameLabel.setMessage(component);
                        }
                        else playerHeadNameLabel.setMessage(
                            Component.literal("Unknown player name").setStyle(Style.EMPTY.withColor(TextColor.GRAY).withItalic(true))
                        );
                    }
                    else playerHeadNameLabel.setMessage(
                        Component.literal("Default").setStyle(Style.EMPTY.withColor(TextColor.GRAY).withItalic(true))
                    );
                }
                playerHeadNameLabel.visible = true;
                editPlayerHeadButton.setMessage(Component.literal("Edit"));
                if (itemStack != null)
                {
                    if (profile != null) itemStack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
                    else itemStack.set(DataComponents.PROFILE, defaultProfile);
                }
            }
        }

        private void updateLayout()
        {
            int playerNameWidth = getWidth() - 4 - editPlayerHeadButton.getWidth();
            if (itemWidget != null)
            {
                playerNameWidth -= 4 + 16;
                itemWidget.setHeight(getHeight());
            }
            playerHeadNameEditBox.setSize(playerNameWidth, getHeight());
            playerHeadNameLabel.setMaxWidth(playerNameWidth);
            editPlayerHeadButton.setHeight(getHeight());
        }

        @Override
        public void setX(int x)
        {
            rootElement.setX(x);
        }

        @Override
        public void setY(int y)
        {
            rootElement.setY(y);
        }

        @Override
        public int getX()
        {
            return rootElement.getX();
        }

        @Override
        public int getY()
        {
            return rootElement.getY();
        }

        @Override
        public int getWidth()
        {
            return 150;
        }

        @Override
        public int getHeight()
        {
            return 20;
        }

        @Override
        public void visitChildren(@NonNull Consumer<LayoutElement> layoutElementVisitor)
        {
            rootElement.visitChildren(layoutElementVisitor);
        }

        @Override
        public void visitWidgets(@NonNull Consumer<AbstractWidget> widgetVisitor)
        {
            rootElement.visitWidgets(widgetVisitor);
        }

        @Override
        public void arrangeElements()
        {
            rootElement.arrangeElements();
        }

        @Override
        public void removeChildren()
        {
            // No!
        }
    }
}