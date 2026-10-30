package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.holdersets.NotHolderSet;
import slimeknights.mantle.data.codec.MantleCodecs;
import slimeknights.mantle.network.codec.MantleStreamCodecs;
import slimeknights.tconstruct.library.codec.TinkerCodecs;
import slimeknights.tconstruct.library.codec.TinkerStreamCodecs;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;

import java.util.stream.Stream;

/** Ingredient matching an {@link net.minecraft.world.item.InstrumentItem} with a particular {@link DataComponents#INSTRUMENT}. */
public record InstrumentIngredient(Item item, HolderSet<Instrument> instrument) implements ICustomIngredient {
  public static final MapCodec<InstrumentIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    MantleCodecs.NON_AIR_ITEM.fieldOf("item").forGetter(InstrumentIngredient::item),
    TinkerCodecs.INSTRUMENT_SET.fieldOf("instrument").forGetter(InstrumentIngredient::instrument)
  ).apply(instance, InstrumentIngredient::new));
  public static final StreamCodec<RegistryFriendlyByteBuf,InstrumentIngredient> STREAM_CODEC = StreamCodec.composite(
    MantleStreamCodecs.ITEM, InstrumentIngredient::item,
    TinkerStreamCodecs.INSTRUMENT_SET, InstrumentIngredient::instrument,
    InstrumentIngredient::new);

  /** Creates a new instance matching the given instrument */
  public static Ingredient of(ItemLike item, HolderSet<Instrument> instrument) {
    return new InstrumentIngredient(item.asItem(), instrument).toVanilla();
  }

  /** Creates a new instance matching the given instrument */
  public static InstrumentIngredient of(ItemLike item, ResourceKey<Instrument> instrument) {
    return new InstrumentIngredient(item.asItem(), HolderSet.direct(BuiltInRegistries.INSTRUMENT.getHolderOrThrow(instrument)));
  }

  /** Creates a new instance ignoring the given tag */
  @SuppressWarnings("deprecation")
  public static InstrumentIngredient of(ItemLike item, TagKey<Instrument> ignore) {
    HolderLookup.RegistryLookup<Instrument> lookup = BuiltInRegistries.INSTRUMENT.asLookup();
    return new InstrumentIngredient(item.asItem(), new NotHolderSet<>(lookup, HolderSet.emptyNamed(lookup, ignore)));
  }


  @Override
  public IngredientType<InstrumentIngredient> getType() {
    return TinkerRecipes.instrumentIngredient.get();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean test(ItemStack stack) {
    if (!stack.is(item)) {
      return false;
    }
    Holder<Instrument> instrument = stack.get(DataComponents.INSTRUMENT);
    return instrument != null && this.instrument.contains(instrument);
  }

  @Override
  public Stream<ItemStack> getItems() {
    return instrument.stream().map(instrument -> {
      ItemStack stack = new ItemStack(item);
      stack.set(DataComponents.INSTRUMENT, instrument);
      return stack;
    });
  }
}
