package one.devos.nautical.losing_my_marbles.fabric;

import java.nio.file.Path;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.maker.MarbleMakerMenu;
import one.devos.nautical.losing_my_marbles.framework.network.ServerPlayPayloadHandler;
import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEnvironment;
import one.devos.nautical.losing_my_marbles.framework.platform.Env;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

public class FabricPlatformHelper implements PlatformHelper {
	@Override
	public Env getEnvironment() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Env.CLIENT : Env.SERVER;
	}

	@Override
	public Path getGameDir() {
		return FabricLoader.getInstance().getGameDir();
	}

	@Override
	public Optional<Path> findPath(String name) {
		return FabricLoader.getInstance().getModContainer(LosingMyMarbles.ID).orElseThrow().findPath(name);
	}

	@Override
	public PhysicsEnvironment getPhysicsEnvironment(ServerLevel level) {
		return level.getAttachedOrCreate(LosingMyMarblesFabric.PHYSICS_ENV_ATTACHMENT, () -> new PhysicsEnvironment(level));
	}

	@Override
	public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super RegistryFriendlyByteBuf, T> registerPlayPayloadC2S(
			CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		return PayloadTypeRegistry.playC2S().register(type, codec);
	}

	@Override
	public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super RegistryFriendlyByteBuf, T> registerPlayPayloadS2C(
			CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		return PayloadTypeRegistry.playS2C().register(type, codec);
	}

	@Override
	public <T extends CustomPacketPayload> void registerPlayPayloadHandler(CustomPacketPayload.Type<T> type, ServerPlayPayloadHandler<T> handler) {
		ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload, context.player()));
	}

	@Override
	public <T> Registry<T> registerStaticRegistry(ResourceKey<Registry<T>> key, @Nullable ResourceLocation defaultKey) {
		FabricRegistryBuilder<T, ? extends Registry<T>> builder = defaultKey == null
				? FabricRegistryBuilder.createSimple(key)
				: FabricRegistryBuilder.createDefaulted(key, defaultKey);
		return builder.attribute(RegistryAttribute.SYNCED).buildAndRegister();
	}

	@Override
	public <T> void registerDynamicRegistry(ResourceKey<Registry<T>> key, Codec<T> codec) {
		DynamicRegistries.registerSynced(key, codec);
	}

	@Override
	public MenuType<MarbleMakerMenu> createMarbleMakerMenuType() {
		return new MenuType<>(MarbleMakerMenu::new, FeatureFlags.VANILLA_SET);
	}

	@Override
	public CreativeModeTab.Builder newCreativeTab() {
		return FabricItemGroup.builder();
	}
}
