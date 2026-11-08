package slimeknights.tconstruct.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.world.TinkerWorld;

import java.util.concurrent.CompletableFuture;

/** Provider for {@link net.neoforged.neoforge.registries.datamaps.DataMapType} entries. */
public class TinkerDataMapProvider extends DataMapProvider {
  public TinkerDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
    super(packOutput, lookupProvider);
  }

  /** Adds a compost recipe */
  @SuppressWarnings("deprecation")
  private void compost(ItemLike item, float chance) {
    builder(NeoForgeDataMaps.COMPOSTABLES).add(item.asItem().builtInRegistryHolder(), new Compostable(chance), false);
  }

  @Override
  protected void gather(HolderLookup.Provider provider) {
    // gadgets
    TinkerGadgets.cake.forEach(block -> compost(block, 1.0f));
    compost(TinkerGadgets.magmaCake, 1.0f);
    // world
    TinkerWorld.slimeLeaves.forEach((type, block) -> compost(block, type.isNether() ? 0.85f : 0.35f));
    TinkerWorld.slimeSapling.forEach(block -> compost(block, 0.35f));
    TinkerWorld.slimeTallGrass.forEach(block -> compost(block, 0.35f));
    TinkerWorld.slimeFern.forEach(block -> compost(block, 0.65f));
    TinkerWorld.slimeGrassSeeds.forEach(block -> compost(block, 0.35F));
    compost(TinkerWorld.skySlimeVine, 0.5f);
    compost(TinkerWorld.enderSlimeVine, 0.5f);
    compost(TinkerWorld.enderbarkRoots, 0.4f);
  }
}
