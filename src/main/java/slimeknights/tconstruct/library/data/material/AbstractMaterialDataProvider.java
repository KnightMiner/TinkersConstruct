package slimeknights.tconstruct.library.data.material;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.crafting.conditions.AndCondition;
import net.neoforged.neoforge.common.crafting.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.conditions.OrCondition;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.tconstruct.library.json.JsonRedirect;
import slimeknights.tconstruct.common.config.ConfigEnabledCondition;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialManager;
import slimeknights.tconstruct.library.materials.json.MaterialJson;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Extendable material provider, useful for addons
 */
@SuppressWarnings({"SameParameterValue", "unused"})
public abstract class AbstractMaterialDataProvider extends GenericDataProvider {
  /** General purpose materials */
  public static final int ORDER_GENERAL = 0;
  /** Materials primarily used for harvest */
  public static final int ORDER_HARVEST = 1;
  /** Materials primarily used for weapons */
  public static final int ORDER_WEAPON = 2;
  /** General purpose materials */
  public static final int ORDER_SPECIAL = 3;
  /** Ranged exclusive materials */
  public static final int ORDER_RANGED = 4;
  /** Order for mod integration materials */
  public static final int ORDER_COMPAT = 5;
  /** Order for nether materials in tiers 1-3 */
  public static final int ORDER_NETHER = 10;
  /** Order for end materials in tiers 1-4 */
  public static final int ORDER_END = 15;
  /** Order for materials that are just a binding */
  public static final int ORDER_BINDING = 20;
  /** Order for materials that are just used for repair or textures */
  public static final int ORDER_REPAIR = 25;

  /** List of all added materials */
  private final Map<MaterialId, MaterialBuilder> allMaterials = new HashMap<>();

  /** Boolean just in case material stats run first */
  private boolean addMaterialsRun = false;

  public AbstractMaterialDataProvider(PackOutput packOutput) {
    super(packOutput, Target.DATA_PACK, MaterialManager.FOLDER, MaterialManager.GSON);
  }

  /**
   * Function to add all relevant materials
   */
  protected abstract void addMaterials();

  private void ensureAddMaterialsRun() {
    if (addMaterialsRun) {
      return;
    }
    addMaterialsRun = true;
    addMaterials();
  }

  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    ensureAddMaterialsRun();
    return allOf(allMaterials.entrySet().stream().map(entry -> saveJson(cache, entry.getKey(), entry.getValue().convert())));
  }

  /**
   * Gets a list of all material IDs that are generated. Note this will run {@link #addMaterials()}, so generally its better to run your material data provider first
   * @return  Material ID list
   */
  public Set<MaterialId> getAllMaterials() {
    ensureAddMaterialsRun();
    // ignore any pure redirects
    return allMaterials.values().stream()
      .filter(e -> !e.isPureRedirect())
      .map(b -> b.id)
      .collect(Collectors.toSet());
  }


  /* Base methods */

  /** Creates a new material builder */
  protected MaterialBuilder material(MaterialId id) {
    return allMaterials.computeIfAbsent(id, MaterialBuilder::new);
  }

  /** Conditions on a forge tag existing */
  protected static ICondition tagExistsCondition(String name) {
    return new TagFilledCondition<>(Registries.ITEM, Mantle.commonResource(name));
  }


  /* Builder */

  @Accessors(fluent = true)
  @Setter
  @CanIgnoreReturnValue
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  protected static class MaterialBuilder {
    private final List<ICondition> conditions = new ArrayList<>();
    private final List<JsonRedirect> redirects = new ArrayList<>();
    private final MaterialId id;
    private boolean craftable = false;
    private boolean hidden = false;
    private int tier = 1;
    private int sort = 100;
    private Rarity rarity = null;

    /** Makes the material craftable in the part builder */
    public MaterialBuilder craftable() {
      craftable = true;
      return this;
    }

    /** Makes the material hidden in recipe viewers and the book */
    public MaterialBuilder hidden() {
      hidden = true;
      return this;
    }


    /* Conditions */

    /** Adds a condition to the builder */
    public MaterialBuilder condition(@Nullable ICondition condition, ICondition... conditions) {
      this.conditions.add(condition);
      Collections.addAll(this.conditions, conditions);
      return this;
    }

    /** Adds a tag exists condition */
    public MaterialBuilder ifTagExists(String name) {
      return condition(tagExistsCondition(name));
    }

    /** Adds the compat material conditions */
    private MaterialBuilder compat(Stream<ICondition> conditions) {
      return condition(new OrCondition(Stream.concat(Stream.of(ConfigEnabledCondition.FORCE_INTEGRATION_MATERIALS), conditions).toArray(ICondition[]::new)));
    }

    /** Adds the compat material conditions */
    public MaterialBuilder compat(ICondition... conditions) {
      return compat(Arrays.stream(conditions));
    }

    /** Adds the compat material conditions */
    public MaterialBuilder compat(String... tagNames) {
      return compat(Arrays.stream(tagNames).map(AbstractMaterialDataProvider::tagExistsCondition));
    }

    /** Adds the compat material condition for a single ingot */
    public MaterialBuilder compatMetal(String ingotName) {
      return compat("ingots/" + ingotName);
    }

    /** Adds the compat material condition for a single ingot name from the material ID */
    public MaterialBuilder compatMetal() {
      return compatMetal(id.getPath());
    }

    /** Adds the compat material condition for the given nested conditions */
    public MaterialBuilder compatAlloy(ICondition... alloyConditions) {
      return condition(new OrCondition(
        // if forced
        ConfigEnabledCondition.FORCE_INTEGRATION_MATERIALS,
        // or we have the matching alloy ingot
        tagExistsCondition("ingots/" + id.getPath()),
        // or we allow ingotless alloys and have all alloy components
        new AndCondition(Util.prepend(alloyConditions, ConfigEnabledCondition.ALLOW_INGOTLESS_ALLOYS))
      ));
    }

    /** Adds the compat material condition for a list of ingot options, alloy is enabled if any of them are enabled */
    public MaterialBuilder compatAlloy(String... ingotNames) {
      ICondition[] conditions = Arrays.stream(ingotNames).map(name -> tagExistsCondition("ingots/" + name)).toArray(ICondition[]::new);
      if (conditions.length == 1) {
        return compatAlloy(conditions[0]);
      }
      return compatAlloy(new OrCondition(conditions));
    }


    /* Redirects */

    /** Checks if this builder is a pure redirect, having no path that will use the rest of the JSON */
    private boolean isPureRedirect() {
      return !redirects.isEmpty() && redirects.get(redirects.size() - 1).getCondition() == null;
    }

    /** Adds a redirect to the builder */
    public MaterialBuilder redirect(JsonRedirect redirect) {
      if (isPureRedirect()) {
        throw new IllegalStateException("Material is already a pure redirect, cannot add another redirect");
      }
      redirects.add(redirect);
      return this;
    }

    /** Adds a redirect to the builder */
    public MaterialBuilder redirect(ResourceLocation id, ICondition... conditions) {
      ICondition combined = null;
      if (conditions.length == 1) {
        combined = conditions[0];
      } else if (conditions.length > 1) {
        combined = new AndCondition(conditions);
      }
      return redirect(new JsonRedirect(id, combined));
    }


    /* Build */

    /** Creates a material JSON */
    @CheckReturnValue
    private MaterialJson convert() {
      ICondition condition = null;
      int count = conditions.size();
      if (count == 1) {
        condition = conditions.get(0);
      } else if (count > 1) {
        condition = new AndCondition(conditions.toArray(new ICondition[0]));
      }
      JsonRedirect[] redirects = null;
      if (!this.redirects.isEmpty()) {
        redirects = this.redirects.toArray(new JsonRedirect[0]);
      }
      // if the array ends in a null condition, skip serializing all material data
      if (isPureRedirect()) {
        return new MaterialJson(condition, null, null, null, null, null, redirects);
      }
      return new MaterialJson(condition, craftable, tier, sort, rarity, hidden, redirects);
    }
  }
}
