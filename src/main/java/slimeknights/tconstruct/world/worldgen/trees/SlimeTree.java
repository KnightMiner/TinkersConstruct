package slimeknights.tconstruct.world.worldgen.trees;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.world.TinkerStructures;
import slimeknights.tconstruct.world.block.FoliageType;

import java.util.Optional;

/** {@link TreeGrower} instances for all slimy saplings. */
public class SlimeTree {
  public static final TreeGrower GREENHEART = tree("greenheart", TinkerStructures.earthSlimeTree);
  public static final TreeGrower SKYROOT = tree("skyroot", TinkerStructures.skySlimeTree);
  public static final TreeGrower BLOODSHROOM = tree("bloodshroom", TinkerStructures.bloodSlimeFungus);
  public static final TreeGrower ICHOR = tree("ichor", TinkerStructures.ichorSlimeFungus);
  public static final TreeGrower ENDERBARK = new TreeGrower(
    TConstruct.resourceString("enderbark"), 0.85f,
    Optional.empty(), Optional.empty(),
    Optional.of(TinkerStructures.enderSlimeTree),
    Optional.of(TinkerStructures.enderSlimeTreeTall),
    Optional.empty(), Optional.empty()
  );

  private SlimeTree() {}

  /** Gets the tree for the given foliage type */
  public static TreeGrower getTree(FoliageType type) {
    return switch (type) {
      case EARTH -> GREENHEART;
      case SKY -> SKYROOT;
      case ICHOR -> ICHOR;
      case BLOOD -> BLOODSHROOM;
      case ENDER -> ENDERBARK;
    };
  }


  /** Creates a standard tinkers tree, with no flowers or mega tree */
  private static TreeGrower tree(String name, ResourceKey<ConfiguredFeature<?,?>> tree) {
    return new TreeGrower(TConstruct.resourceString(name), Optional.empty(), Optional.of(tree), Optional.empty());
  }
}
