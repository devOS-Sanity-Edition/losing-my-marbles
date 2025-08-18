package one.devos.nautical.losing_my_marbles.fabric.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.SlotDisplay;

@Mixin(GhostSlots.class)
public interface GhostSlotsAccessor {
	@Invoker
	void callSetInput(Slot slot, ContextMap contextMap, SlotDisplay slotDisplay);

	@Invoker
	void callSetResult(Slot slot, ContextMap contextMap, SlotDisplay slotDisplay);
}
