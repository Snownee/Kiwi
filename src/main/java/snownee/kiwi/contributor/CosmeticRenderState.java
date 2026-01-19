package snownee.kiwi.contributor;

import org.jspecify.annotations.Nullable;

import snownee.kiwi.contributor.client.CosmeticLayer;

public interface CosmeticRenderState {
	@Nullable CosmeticLayer kiwi$getCosmeticLayer();

	void kiwi$setCosmeticLayer(CosmeticLayer layer);
}
