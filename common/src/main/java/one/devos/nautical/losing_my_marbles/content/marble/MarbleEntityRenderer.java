package one.devos.nautical.losing_my_marbles.content.marble;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class MarbleEntityRenderer extends EntityRenderer<MarbleEntity, MarbleEntityRenderState> {
	public MarbleEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void extractRenderState(MarbleEntity marble, MarbleEntityRenderState state, float $$2) {
		super.extractRenderState(marble, state, $$2);

	}

	@Override
	public MarbleEntityRenderState createRenderState() {
		return new MarbleEntityRenderState();
	}
}
