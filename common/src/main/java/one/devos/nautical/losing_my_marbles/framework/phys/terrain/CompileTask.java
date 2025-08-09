package one.devos.nautical.losing_my_marbles.framework.phys.terrain;

import java.util.concurrent.CompletableFuture;

public record CompileTask(SectionShapeCompiler compiler, CompletableFuture<CompiledSection> future) {
}
