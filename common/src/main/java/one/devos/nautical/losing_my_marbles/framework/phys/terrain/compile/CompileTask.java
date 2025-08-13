package one.devos.nautical.losing_my_marbles.framework.phys.terrain.compile;

import java.util.concurrent.CompletableFuture;

public record CompileTask(SectionShapeCompiler compiler, CompletableFuture<CompiledSection> future) {
	public void discardResult() {
		this.future.thenAccept(compiled -> {
			for (SectionShape shape : compiled.shapes()) {
				shape.shape().close();
			}
		});
	}
}
