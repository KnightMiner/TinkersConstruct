package slimeknights.tconstruct.library.data.tinkering;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.data.GenericRegistryDataProvider;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.tconstruct.library.json.JsonRedirect;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.modifiers.impl.ComposableModifier;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Datagen for dynamic modifiers */
@SuppressWarnings("SameParameterValue")
public abstract class AbstractModifierProvider extends GenericRegistryDataProvider {
  private final Map<ModifierId,Composable> composableModifiers = new HashMap<>();

  public AbstractModifierProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
    super(packOutput, ModifierManager.FOLDER, registries);
  }

  /**
   * Function to add all relevant modifiers
   */
  protected abstract void addModifiers(HolderLookup.Provider registries);

  /** Adds the given builder, handling duplicate modifiers */
  private void addBuilder(ModifierId id, @Nullable ComposableModifier.Builder builder, List<ICondition> conditions, JsonRedirect... redirects) {
    Composable previous = composableModifiers.putIfAbsent(id, new Composable(builder, conditions, redirects));
    if (previous != null) {
      throw new IllegalArgumentException("Duplicate modifier " + id);
    }
  }

  /* Composable helpers */

  /** Sets up a builder for a composable modifier */
  protected ComposableModifier.Builder buildModifier(ModifierId id, List<ICondition> conditions, JsonRedirect... redirects) {
    ComposableModifier.Builder builder = ComposableModifier.builder();
    addBuilder(id, builder, conditions, redirects);
    return builder;
  }

  /** Sets up a builder for a composable modifier */
  protected ComposableModifier.Builder buildModifier(ModifierId id, ICondition... conditions) {
    return buildModifier(id, List.of(conditions));
  }

  /** Sets up a builder for a composable modifier */
  protected ComposableModifier.Builder buildModifier(ModifierHolder modifier, List<ICondition> conditions, JsonRedirect... redirects) {
    return buildModifier(modifier.id(), conditions, redirects);
  }

  /** Sets up a builder for a composable modifier */
  protected ComposableModifier.Builder buildModifier(ModifierHolder modifier, JsonRedirect... redirects) {
    return buildModifier(modifier, List.of(), redirects);
  }


  /* Redirect helpers */

  /** Adds a redirect with no modifier modules */
  protected void addRedirect(ModifierId id, List<ICondition> conditions, JsonRedirect... redirects) {
    addBuilder(id, null, conditions, redirects);
  }

  /** Adds a modifier redirect */
  protected void addRedirect(ModifierId id, JsonRedirect... redirects) {
    addRedirect(id, List.of(), redirects);
  }

  /** Makes a conditional redirect to the given ID */
  protected JsonRedirect conditionalRedirect(ModifierId id, @Nullable ICondition condition) {
    return new JsonRedirect(id.id(), condition);
  }

  /** Makes an unconditional redirect to the given ID */
  protected JsonRedirect redirect(ModifierId id) {
    return conditionalRedirect(id, null);
  }

  @Override
  public CompletableFuture<?> run(CachedOutput cache, HolderLookup.Provider registries) {
    addModifiers(registries);
    return allOf(composableModifiers.entrySet().stream().map(entry -> {
      try {
        ModifierId id = entry.getKey();
        return saveJson(cache, id.id(), entry.getValue().serialize(registries, id));
      } catch (RuntimeException e) {
        throw new RuntimeException("Failed to serialize modifier " + entry.getKey(), e);
      }
    }));
  }

  /** Result for composable too */
  private record Composable(@Nullable ComposableModifier.Builder builder, List<ICondition> conditions, JsonRedirect[] redirects) {
    /** Writes this result to JSON */
    public JsonObject serialize(HolderLookup.Provider registries, ModifierId id) {
      TypedMap context = ModifierManager.contextBuilder(registries, id).build();
      JsonObject json;
      if (builder != null) {
        json = ComposableModifier.LOADER.serialize(builder.build(), context).getAsJsonObject();
      } else {
        json = new JsonObject();
      }
      if (redirects.length != 0) {
        JsonArray array = new JsonArray();
        for (JsonRedirect redirect : redirects) {
          array.add(redirect.toJson());
        }
        json.add("redirects", array);
      }
      JsonHelper.serializeConditions(json, conditions);
      return json;
    }
  }
}
