package one.devos.nautical.losing_my_marbles.content;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.maker.MarbleMakerMenu;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

public final class LosingMyMarblesMenus {
	public static final MenuType<MarbleMakerMenu> MARBLE_MAKER = register("marble_maker", PlatformHelper.INSTANCE.createMarbleMakerMenuType());

	private static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType<T> menuType) {
		return Registry.register(BuiltInRegistries.MENU, LosingMyMarbles.id(name), menuType);
	}

	public static void init() {
	}
}
