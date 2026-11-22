package slimeknights.tconstruct.library.recipe.modifiers.adding.variant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.tools.component.ResourceMap;

/** {@link SwappableVariant} using {@link TinkerData#MODIFIER_STRING} to set strings. */
public record SwappableString(String value) implements SwappableModifierMap<String> {
  /** Codec that validates the variant string */
  public static final Codec<String> VALID_CODEC = ExtraCodecs.NON_EMPTY_STRING.validate(string -> isValid(string) ? DataResult.success(string) : DataResult.error(() -> "Non [a-z0-9_.-] character in modifier variant string: " + string));
  /** Loadable that validates the variant string */
  public static final StringLoadable<String> VALID_LOADABLE = StringLoadable.NON_EMPTY.validate((string, error) -> {
    if (isValid(string)) {
      return string;
    }
    throw error.create("Non [a-z0-9_.-] character in modifier variant string: " + string);
  });
  /** Loader for this class */
  public static final RecordLoadable<SwappableString> LOADER = RecordLoadable.create(VALID_LOADABLE.requiredField("value", SwappableString::value), SwappableString::new);

  @Override
  public RecordLoadable<? extends GenericLoaderRegistry.IHaveLoader> getLoader() {
    return LOADER;
  }

  @Override
  public DataComponentType<ResourceMap<String>> component() {
    return TinkerData.MODIFIER_STRING.get();
  }

  @Override
  public Component formatVariant(ModifierHolder modifier) {
    return VariantFormatter.STRING.format(modifier, value);
  }

  /** Checks if the given string is valid */
  public static boolean isValid(String string) {
    return ResourceLocation.isValidNamespace(string);
  }
}
