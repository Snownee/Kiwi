package snownee.kiwi.block.entity;

import java.util.Set;

import com.mojang.datafixers.types.Type;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.Factory;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TagBasedBlockEntityType<T extends BlockEntity> extends BlockEntityType<T> {

	private final TagKey<Block> tag;

	public TagBasedBlockEntityType(Factory<? extends T> factory, TagKey<Block> tag, Type<?> datafixer) {
		super(factory::create, Set.of(), datafixer);
		this.tag = tag;
	}

	@Override
	public boolean isValid(BlockState state) {
		return state.is(tag);
	}

}