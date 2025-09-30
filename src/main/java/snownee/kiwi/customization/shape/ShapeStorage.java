package snownee.kiwi.customization.shape;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.shapes.Shapes;
import snownee.kiwi.Kiwi;
import snownee.kiwi.loader.Platform;

public class ShapeStorage {
//	private static Map<ResourceLocation, ShapeGenerator> tempResolved;

	private final ImmutableMap<ResourceLocation, ShapeGenerator> shapes;
	private final Map<Pair<ShapeGenerator, Object>, ShapeGenerator> transformed = Maps.newHashMap();

	public ShapeStorage(Map<ResourceLocation, ShapeGenerator> shapes) {
		this.shapes = ImmutableMap.copyOf(shapes);
	}

	@Nullable
	public ShapeGenerator get(ResourceLocation id) {
		return this.shapes.get(id);
	}

	public static ShapeStorage reload(Supplier<Map<ResourceLocation, UnbakedShape>> shapesSupplier) {
		Map<ResourceLocation, UnbakedShape> shapes = Platform.isDataGen() ? Maps.newHashMap() : shapesSupplier.get();
		shapes.put(new ResourceLocation("empty"), new UnbakedShape.Inlined(Shapes.empty()));
		shapes.put(new ResourceLocation("block"), new UnbakedShape.Inlined(Shapes.block()));
		BakingContext.Impl context = new BakingContext.Impl(shapes);
		LinkedHashSet<ShapeRef> refs = Sets.newLinkedHashSet();
		List<UnresolvedEntry> unresolved = shapes.entrySet().stream().map(entry -> {
					UnbakedShape unbakedShape = entry.getValue();
					Set<ShapeRef> dependencies = collectDependencies(unbakedShape)
							.filter($ -> $ != unbakedShape)
							.filter(ShapeRef.class::isInstance)
							.map(ShapeRef.class::cast)
							.collect(Collectors.toSet());
					if (dependencies.isEmpty()) {
						context.bake(entry.getKey(), unbakedShape);
						return null;
					}
					return new UnresolvedEntry(entry.getKey(), unbakedShape, dependencies);
				})
				.filter(Objects::nonNull)
				.filter(entry -> {
					boolean success = true;
					for (ShapeRef ref : entry.dependencies) {
						if (!shapes.containsKey(ref.id())) {
							Kiwi.LOGGER.error("Shape %s depends on %s, but it's not found".formatted(entry.key, ref.id()));
							success = false;
						}
					}
					if (success) {
						refs.addAll(entry.dependencies);
					}
					return success;
				})
				.collect(Collectors.toCollection(LinkedList::new));
		while (!unresolved.isEmpty()) {
			boolean changed = false;
			refs.removeIf(ref -> ref.bindValue(context));
			Iterator<UnresolvedEntry> iterator = unresolved.iterator();
			while (iterator.hasNext()) {
				UnresolvedEntry entry = iterator.next();
				Set<ShapeRef> dependencies = entry.dependencies;
				if (dependencies.stream().allMatch(ShapeRef::isResolved)) {
					context.bake(entry.key, entry.unbakedShape);
					iterator.remove();
					changed = true;
				}
			}
			if (!changed) {
				Kiwi.LOGGER.error("Failed to resolve shapes: %s".formatted(unresolved.stream()
						.map(entry -> entry.key.toString())
						.collect(Collectors.joining(", "))));
				break;
			}
		}
//		injectLegacyShapes(context.byId);
		return new ShapeStorage(context.byId);
	}

	private static Stream<UnbakedShape> collectDependencies(UnbakedShape shape) {
		return Stream.concat(Stream.of(shape), shape.dependencies().flatMap(ShapeStorage::collectDependencies));
	}

//	private static void injectLegacyShapes(Map<ResourceLocation, ShapeGenerator> resolved) {
//		tempResolved = resolved;
//		put("minecraft:carpet", box(0, 0, 0, 16, 1, 16));
//		put("small_book_stack", box(2, 0, 2, 14, 8, 14));
//		put("big_book_stack", box(0, 0, 0, 16, 10, 16));
//		put("bottle_stack", box(2, 0, 2, 14, 8, 14));
//		put("solar_system_model", box(0, 0, 0, 16, 10, 16));
//		put("factory_ceiling_lamp", box(0, 12, 0, 16, 16, 16));
//		put("factory_pendant", box(2, 4, 2, 14, 16, 14));
//
//		VoxelShape TABLE_BASE = box(4, 0, 4, 12, 3, 12);
//		VoxelShape TABLE_LEG = box(6, 3, 6, 10, 13, 10);
//		VoxelShape TABLE_TOP = box(0, 13, 0, 16, 16, 16);
//		put("table", Shapes.or(TABLE_BASE, TABLE_LEG, TABLE_TOP));
//
//		VoxelShape BIG_TABLE_TOP = box(0, 8, 0, 16, 16, 16);
//		VoxelShape BIG_TABLE_LEG_NN = box(0, 0, 0, 2, 8, 2);
//		VoxelShape BIG_TABLE_LEG_NP = box(0, 0, 14, 2, 8, 16);
//		VoxelShape BIG_TABLE_LEG_PN = box(14, 0, 0, 16, 8, 2);
//		VoxelShape BIG_TABLE_LEG_PP = box(14, 0, 14, 16, 8, 16);
//		put("big_table", Shapes.or(BIG_TABLE_TOP, BIG_TABLE_LEG_PP, BIG_TABLE_LEG_PN, BIG_TABLE_LEG_NP, BIG_TABLE_LEG_NN));
//
//		put("long_stool", box(0, 0, 3, 16, 10, 13));
//		put("chair", Shapes.or(box(2, 0, 2, 14, 10, 14), box(2, 10, 12, 14, 16, 14)));
//		put("shelf", Shapes.or(
//				box(15, 0, 0, 16, 16, 16),
//				box(0, 0, 0, 1, 16, 16),
//				box(1, 15, 0, 15, 16, 16),
//				box(1, 0, 0, 15, 1, 16)));
//		put("miniature", box(0, 0, 3, 16, 6, 13));
//		put("teapot", box(4, 0, 4, 12, 6, 12));
//		put("tea_ware", box(0, 0, 3, 16, 2, 13));
//		put("board", box(1, 0, 1, 15, 1, 15));
//		put("porcelain", box(2, 0, 2, 14, 16, 14));
//		put("porcelain_small", box(5, 0, 5, 11, 12, 11));
//		put("lantern", Shapes.or(box(2, 2, 2, 14, 14, 14), box(5, 0, 5, 11, 16, 11)));
//		put("festival_lantern", Shapes.or(box(2, 2, 2, 14, 14, 14), box(5, 0, 5, 11, 16, 11), box(0, 3, 0, 16, 13, 16)));
//		put("candlestick", box(5, 0, 5, 11, 13, 11));
//		put("big_candlestick", box(2, 0, 2, 14, 14, 14));
//		put("covered_lamp", box(4, 0, 4, 12, 16, 12));
//		put("stone_lamp", box(3, 0, 3, 13, 16, 13));
//		put("water_bowl", box(0, 0, 0, 16, 5, 16));
//		put("fish_bowl", box(1, 0, 1, 15, 6, 15));
//		put("fish_tank", Shapes.join(Shapes.block(), box(1, 1, 1, 15, 16, 15), BooleanOp.ONLY_FIRST));
//		put("water_tank", Shapes.join(box(1, 0, 1, 15, 16, 15), box(3, 3, 3, 13, 16, 13), BooleanOp.ONLY_FIRST));
//		put("oil_lamp", box(5, 4, 8, 11, 12, 16));
//		put("empty_candlestick", box(5, 5, 5, 11, 16, 16));
//		put("factory_lamp", box(4, 0, 4, 12, 8, 12));
//		put("fan", box(1, 0, 1, 15, 6, 15));
//		put("screen", box(0, 0, 0, 16, 2, 16));
//		put("wide_screen", box(0, 0, 13, 16, 16, 14));
//		put("vent_fan", box(0, 0, 2, 16, 16, 14));
//		put("tech_table", Shapes.or(box(2, 0, 2, 14, 10, 14), box(0, 10, 0, 16, 16, 16)));
//		put("hologram_base", box(1, 0, 1, 15, 2, 15));
//		put("maya_crystal_skull", Shapes.or(box(2, 0, 2, 14, 2, 14), box(4, 2, 4, 12, 10, 12)));
//		put("dessert", box(1, 0, 1, 15, 2, 15));
//		put("ladder", box(0, 0, 13, 16, 16, 16));
//		put("safety_ladder", Shapes.join(Shapes.block(), box(1, 0, 1, 15, 16, 13), BooleanOp.ONLY_FIRST));
//		put("hollow_steel_half_beam_floor", box(3, -4, 0, 13, 4, 16));
//		put("hollow_steel_half_beam_ceiling", box(3, 12, 0, 13, 20, 16));
//		put("hollow_steel_half_beam_wall", box(3, 0, 12, 13, 16, 20));
//		put("factory_light_bar", box(13, 2, 0, 16, 6, 16));
//		put("wall_base", Shapes.or(
//				box(2, 0, 0, 16, 8, 16),
//				box(5, 8, 0, 16, 13, 16),
//				box(3, 13, 0, 16, 16, 16)));
//		put("wall_base2", Shapes.or(
//				box(2, 8, 0, 16, 16, 16),
//				box(5, 3, 0, 16, 8, 16),
//				box(3, 0, 0, 16, 3, 16)));
//		VoxelShape meirenKao = Shapes.or(
//				box(0, 0, 0, 16, 8, 16),
//				box(8, 8, 0, 16, 16, 16));
//		put("meiren_kao", meirenKao);
//		VoxelShape woodenColumn = box(4, 0, 4, 12, 16, 12);
//		put("wooden_column", box(4, 0, 4, 12, 16, 12));
//		put("meiren_kao_with_column", Shapes.or(meirenKao, woodenColumn));
////		put("fallen_leaves", box(0, 0, 0, 16, 1, 16));
//		put("stone_column", box(2, 0, 2, 14, 16, 14));
//		put("wooden_fence_head", box(6, 0, 6, 10, 8, 10));
//		put("hanging_fascia", box(0, 0, 7, 16, 16, 9));
//		put("cup_1", box(6, 0, 6, 10, 6, 10));
//		put("cup_2", box(3, 0, 3, 13, 6, 13));
//		put("cup_3", box(2, 0, 2, 14, 6, 14));
//		put("cup_4", box(2, 0, 2, 14, 7, 14));
//
//		VoxelShape air_duct_oblique = Shapes.join(
//				Shapes.or(
//						box(0, 0, 12, 16, 16, 16),
//						box(0, -2, 10, 16, 16, 12),
//						box(0, -4, 8, 16, 16, 10),
//						box(0, -6, 6, 16, 16, 8),
//						box(0, -8, 4, 16, 16, 6),
//						box(0, -10, 2, 16, 14, 4),
//						box(0, -12, 0, 16, 12, 2)),
//				Shapes.or(
//						box(2, 2, 12, 14, 14, 16),
//						box(2, 0, 10, 14, 14, 12),
//						box(2, -2, 8, 14, 14, 10),
//						box(2, -4, 6, 14, 14, 8),
//						box(2, -6, 4, 14, 14, 6),
//						box(2, -8, 2, 14, 12, 4),
//						box(2, -10, 0, 14, 10, 2)),
//				BooleanOp.ONLY_FIRST);
//		VoxelShape air_duct_oblique2 = Shapes.join(
//				Shapes.or(
//						box(0, 0, 12, 16, 16, 16),
//						box(0, 0, 10, 16, 18, 12),
//						box(0, 0, 8, 16, 20, 10),
//						box(0, 0, 6, 16, 22, 8),
//						box(0, 0, 4, 16, 24, 6),
//						box(0, 2, 2, 16, 26, 4),
//						box(0, 4, 0, 16, 28, 2)),
//				Shapes.or(
//						box(2, 2, 12, 14, 14, 16),
//						box(2, 2, 10, 14, 16, 12),
//						box(2, 2, 8, 14, 18, 10),
//						box(2, 2, 6, 14, 20, 8),
//						box(2, 2, 4, 14, 22, 6),
//						box(2, 4, 2, 14, 24, 4),
//						box(2, 6, 0, 14, 26, 2)),
//				BooleanOp.ONLY_FIRST);
//		resolved.put(
//				XKDeco.id("air_duct_oblique"),
//				ChoicesShape.chooseOneProperty(
//						XKDStateProperties.HALF,
//						Map.of("upper", ShapeGenerator.unit(air_duct_oblique), "lower", ShapeGenerator.unit(air_duct_oblique2))));
//
//		put("air_duct_oblique_lower", air_duct_oblique2);
//		put("air_duct_oblique_upper", air_duct_oblique);
//
//		VoxelShape airDuctBase = Shapes.join(
//				Shapes.block(),
//				Shapes.or(box(0, 2, 2, 16, 14, 14), box(2, 0, 2, 14, 16, 14), box(2, 2, 0, 14, 14, 16)),
//				BooleanOp.ONLY_FIRST);
//		VoxelShape airDuctSide = box(2, 0, 2, 14, 2, 14);
//		resolved.put(
//				XKDeco.id("air_duct"),
//				SixWayShape.create(
//						ShapeGenerator.unit(airDuctBase),
//						ShapeGenerator.unit(Shapes.empty()),
//						ShapeGenerator.unit(airDuctSide)));
//
//		put("air_duct_base", airDuctBase);
//		put("air_duct_side", airDuctSide);
//	}
//
//	private static void put(String id, VoxelShape shape) {
//		tempResolved.put(Util.RL(id, XKDeco.ID), ShapeGenerator.unit(shape));
//	}

	public void forEach(BiConsumer<? super ResourceLocation, ? super ShapeGenerator> action) {
		shapes.forEach(action);
	}

	public ShapeGenerator transform(ShapeGenerator shape, Object key, UnaryOperator<ShapeGenerator> factory) {
		Pair<ShapeGenerator, Object> pair = Pair.of(shape, key);
		if (transformed.containsKey(pair)) {
			return transformed.get(pair);
		} else {
			ShapeGenerator result = factory.apply(shape);
			transformed.put(pair, result);
			return result;
		}
	}

	private record UnresolvedEntry(ResourceLocation key, UnbakedShape unbakedShape, Set<ShapeRef> dependencies) {
	}
}
