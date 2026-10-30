package slimeknights.tconstruct.library.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.codec.MantleCodecs;

import java.util.function.Function;
import java.util.stream.Stream;

/** This class contains codecs for various vanilla things that we need to use in codecs. */
public class TinkerCodecs {
  /** Codec for serializing a vanilla ingredient directly into a modded ingredient. Will serialize as "item" or "tag" if simple, or as "match" if complex */
  public static final MapCodec<Ingredient> NESTED = Codec.mapEither(
    Ingredient.CODEC.fieldOf("match"),
    Ingredient.Value.MAP_CODEC
  ).xmap(
    either -> either.map(Function.identity(), value -> Ingredient.fromValues(Stream.of(value))),
    ingredient -> {
      // cannot serialize custom - conflicting types
      if (ingredient.isCustom()) {
        return Either.left(ingredient);
      }
      // list means we need to nest it anyway
      Ingredient.Value[] values = ingredient.getValues();
      return values.length != 1 ? Either.left(ingredient) : Either.right(values[0]);
    }
  );

  /** Codec for a holder set of instruments. Used in {@link slimeknights.tconstruct.library.recipe.ingredient.InstrumentIngredient} */
  public static final Codec<HolderSet<Instrument>> INSTRUMENT_SET = MantleCodecs.holderSet(BuiltInRegistries.INSTRUMENT);
}
