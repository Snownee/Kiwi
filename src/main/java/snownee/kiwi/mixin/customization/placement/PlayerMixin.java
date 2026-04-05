package snownee.kiwi.mixin.customization.placement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.player.Player;
import snownee.kiwi.customization.duck.KPlayer;

@Mixin(Player.class)
public class PlayerMixin implements KPlayer {
	@Unique
	private int kiwi$placeCount;

	@Override
	public void kiwi$setPlaceCount(int i) {
		this.kiwi$placeCount = i;
	}

	@Override
	public int kiwi$getPlaceCount() {
		return kiwi$placeCount;
	}
}
