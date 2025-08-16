package one.devos.nautical.losing_my_marbles.content.marble.asset;

import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;

public final class MarbleAssetManager extends SimpleJsonResourceReloadListener<MarbleAsset> {
	public static final ResourceLocation ID = LosingMyMarbles.id("marble_assets");
	private static final FileToIdConverter ASSET_LISTER = FileToIdConverter.json("marbles");
	public static final MarbleAssetManager INSTANCE = new MarbleAssetManager();

	private Map<ResourceKey<MarbleAsset>, MarbleAsset> marbleAssets = Map.of();

	private MarbleAssetManager() {
		super(MarbleAsset.CODEC, ASSET_LISTER);
	}

	@Override
	protected void apply(Map<ResourceLocation, MarbleAsset> marbleAssets, ResourceManager manager, ProfilerFiller profiler) {
		this.marbleAssets = marbleAssets.entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(entry -> ResourceKey.create(MarbleAsset.REGISTRY_KEY, entry.getKey()), Map.Entry::getValue));
	}

	@Nullable
	public MarbleAsset get(ResourceKey<MarbleAsset> key) {
		return this.marbleAssets.get(key);
	}
}
