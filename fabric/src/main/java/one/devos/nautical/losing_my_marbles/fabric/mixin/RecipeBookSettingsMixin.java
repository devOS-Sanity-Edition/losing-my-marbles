package one.devos.nautical.losing_my_marbles.fabric.mixin;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.stats.RecipeBookSettings.TypeSettings;
import net.minecraft.world.inventory.RecipeBookType;
import one.devos.nautical.losing_my_marbles.content.marble.recipeBook.MarbleMakerRecipeBookHelper;
import one.devos.nautical.losing_my_marbles.content.marble.recipeBook.MarbleRecipeBookSettings;
import one.devos.nautical.losing_my_marbles.framework.extension.RecipeBookSettingsExt;

// Chosen by fair dice roll, guaranteed to be random
@Mixin(value = RecipeBookSettings.class, priority = 544)
public class RecipeBookSettingsMixin implements RecipeBookSettingsExt {
	@Unique
	private TypeSettings lmm$marbleMaker = TypeSettings.DEFAULT;

	@ModifyReturnValue(method = "method_71331", at = @At("RETURN"))
	private static App<RecordCodecBuilder.Mu<RecipeBookSettings>, RecipeBookSettings> lmm$addFieldForMarbleMakerInCodec(App<RecordCodecBuilder.Mu<RecipeBookSettings>, RecipeBookSettings> original,
																														RecordCodecBuilder.Instance<RecipeBookSettings> instance) {
		return instance.group(
				original,
				MarbleRecipeBookSettings.MARBLE_MAKER_MAP_CODEC.forGetter(s -> ((RecipeBookSettingsExt) (Object) s).lmm$getMarbleMakerTypeSettings())
		).apply(instance, (s, marbleMaker) -> {
			((RecipeBookSettingsExt) (Object) s).lmm$setMarbleMakerTypeSettings(marbleMaker);
			return s;
		});
	}

	@ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;composite(Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;)Lnet/minecraft/network/codec/StreamCodec;"))
	private static StreamCodec<FriendlyByteBuf, RecipeBookSettings> lmm$addFieldForMarbleMakerInStreamCodec(StreamCodec<FriendlyByteBuf, RecipeBookSettings> original) {
		return StreamCodec.composite(
				original, Function.identity(),
				TypeSettings.STREAM_CODEC, s -> ((RecipeBookSettingsExt) (Object) s).lmm$getMarbleMakerTypeSettings(),
				(s, marbleMaker) -> {
					((RecipeBookSettingsExt) (Object) s).lmm$setMarbleMakerTypeSettings(marbleMaker);
					return s;
				}
		);
	}

	@Inject(method = "getSettings", at = @At("HEAD"), cancellable = true)
	private void lmm$getSettingsForMarbleMaker(RecipeBookType recipeBookType, CallbackInfoReturnable<TypeSettings> cir) {
		if (recipeBookType == MarbleMakerRecipeBookHelper.MARBLE_MAKER) {
			cir.setReturnValue(this.lmm$marbleMaker);
		}
	}

	@Inject(method = "updateSettings", at = @At("HEAD"), cancellable = true)
	private void lmm$updateSettingsForMarbleMaker(RecipeBookType recipeBookType, UnaryOperator<TypeSettings> unaryOperator, CallbackInfo ci) {
		if (recipeBookType == MarbleMakerRecipeBookHelper.MARBLE_MAKER) {
			this.lmm$marbleMaker = unaryOperator.apply(this.lmm$marbleMaker);
			ci.cancel();
		}
	}

	@ModifyReturnValue(method = "copy", at = @At("RETURN"))
	private RecipeBookSettings lmm$copyForMarbleMaker(RecipeBookSettings original) {
		((RecipeBookSettingsExt) (Object) original).lmm$setMarbleMakerTypeSettings(this.lmm$marbleMaker);
		return original;
	}

	@Inject(method = "replaceFrom", at = @At("HEAD"))
	private void lmm$replaceFromForMarbleMaker(RecipeBookSettings recipeBookSettings, CallbackInfo ci) {
		this.lmm$marbleMaker = ((RecipeBookSettingsExt) (Object) recipeBookSettings).lmm$getMarbleMakerTypeSettings();
	}

	@Override
	public TypeSettings lmm$getMarbleMakerTypeSettings() {
		return this.lmm$marbleMaker;
	}

	@Override
	public void lmm$setMarbleMakerTypeSettings(TypeSettings marbleMaker) {
		this.lmm$marbleMaker = marbleMaker;
	}
}
