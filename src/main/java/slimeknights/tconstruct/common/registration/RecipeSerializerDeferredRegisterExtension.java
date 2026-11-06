package slimeknights.tconstruct.common.registration;

import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.registration.deferred.RecipeSerializerDeferredRegister;
import slimeknights.tconstruct.common.registration.object.CastingRecipeSerializers;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;

import java.util.function.Function;

/** Extension of {@link RecipeSerializerDeferredRegister} with helpers for casting */
public class RecipeSerializerDeferredRegisterExtension extends RecipeSerializerDeferredRegister {
  public RecipeSerializerDeferredRegisterExtension(String namespace) {
    super(namespace);
  }

  /** Registers a pair of casting table and basin serializers using the given loadable */
  public <T extends ICastingRecipe> CastingRecipeSerializers<T> registerCasting(Function<String,String> nameGetter, RecordLoadable<T> loadable) {
    return new CastingRecipeSerializers<>(
      registerLoadable(nameGetter.apply("table"), loadable, TinkerRecipes.castingTableType),
      registerLoadable(nameGetter.apply("basin"), loadable, TinkerRecipes.castingBasinType)
    );
  }

  /** Registers a pair of casting table with the given suffix */
  public <T extends ICastingRecipe> CastingRecipeSerializers<T> registerCastingPrefix(String prefix, RecordLoadable<T> loadable) {
    return registerCasting(type -> prefix + '_' + type, loadable);
  }

  /** Registers a pair of casting table with the given suffix */
  public <T extends ICastingRecipe> CastingRecipeSerializers<T> registerCastingSuffix(String suffix, RecordLoadable<T> loadable) {
    return registerCasting(type -> type + '_' + suffix, loadable);
  }
}
