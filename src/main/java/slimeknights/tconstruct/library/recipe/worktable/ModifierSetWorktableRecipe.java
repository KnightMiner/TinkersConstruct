package slimeknights.tconstruct.library.recipe.worktable;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.TinkerLoadables;
import slimeknights.tconstruct.library.json.predicate.modifier.ModifierPredicate;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.TinkerableRecipeInput;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.tools.component.SetComponent;
import slimeknights.tconstruct.library.tools.stack.LazyToolStack;
import slimeknights.tconstruct.library.tools.stack.ToolStack;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/** Recipe to add or remove a modifier from a set in persistent data */
public class ModifierSetWorktableRecipe extends AbstractWorktableRecipe {
  /** Codec instance for a modifier set. Only components that use this specific instance may be used in these recipes */
  public static final Codec<SetComponent<ModifierId>> MODIFIER_SET_CODEC = SetComponent.codec(ModifierId.CODEC);
  /** Stream codec instance for a modifier set. */
  public static final StreamCodec<ByteBuf, SetComponent<ModifierId>> MODIFIER_SET_STREAM_CODEC = SetComponent.streamCodec(ModifierId.STREAM_CODEC);

  /** Message to display if there are no matching modifiers on the tool */
  public static final Component NO_MATCHES = TConstruct.makeTranslation("recipe", "modifier_set_worktable.empty");
  /** Logic to fetch a list of strings from the persistent data */
  public static final BiFunction<CompoundTag, String, ListTag> LIST_GETTER = (tag, name) -> tag.getList(name, Tag.TAG_STRING);
  /** Loader instance */
  public static final RecordLoadable<ModifierSetWorktableRecipe> LOADER = RecordLoadable.create(
    TinkerLoadables.componentWithCodec(MODIFIER_SET_CODEC, "modifier set").requiredField("component", r -> r.component),
    INPUTS_FIELD, TOOL_FIELD,
    // TODO: move modifier predicate to base recipe
    ModifierPredicate.LOADER.defaultField("modifier_predicate", false, r -> r.modifierPredicate),
    // TODO: switch to an enum with ADD, REMOVE, and TOGGLE options
    BooleanLoadable.INSTANCE.requiredField("add_to_set", r -> r.addToSet),
    BooleanLoadable.INSTANCE.defaultField("allow_traits", false, r -> r.allowTraits),
    ModifierSetWorktableRecipe::new);

  /** Title to display in the UI and JEI */
  @Getter
  private final Component title;
  /** Description to display when valid */
  private final Component description;
  /** Key of the set to fill with modifier names */
  private final DataComponentType<SetComponent<ModifierId>> component;
  /** Predicate of modifiers to support in this recipe */
  private final IJsonPredicate<ModifierId> modifierPredicate;
  /** Filter of modifiers to display */
  private final Predicate<ModifierEntry> entryFilter;
  /** If true, adds the matched modifier to the set, if false removes it */
  private final boolean addToSet;
  /** If true, traits can be targeted. If false, only recipe modifiers */
  private final boolean allowTraits;
  /** Cached list of modifiers shown in JEI */
  private List<ModifierEntry> filteredModifiers = null;

  public ModifierSetWorktableRecipe(DataComponentType<SetComponent<ModifierId>> component, List<SizedIngredient> inputs, Ingredient toolRequirement, IJsonPredicate<ModifierId> modifierPredicate, boolean addToSet, boolean allowTraits) {
    super(toolRequirement, inputs);
    this.component = component;
    this.addToSet = addToSet;
    String rootKey = Util.makeTranslationKey("recipe", Loadables.DATA_COMPONENT_TYPE.getKey(component)) + (addToSet ? ".adding" : ".removing");
    this.title = Component.translatable(rootKey + ".title");
    this.description = Component.translatable(rootKey + ".description");
    this.modifierPredicate = modifierPredicate;
    this.entryFilter = entry -> modifierPredicate.matches(entry.id());
    this.allowTraits = allowTraits;
  }

  /** Gets the modifiers from the container */
  private List<ModifierEntry> getModifiers(TinkerableRecipeInput inv) {
    if (allowTraits) {
      return inv.getTinkerable().getModifiers().modifiers();
    }
    return inv.getTinkerable().getCraftedModifiers().modifiers();
  }

  @Override
  public Component getDescription(@Nullable TinkerableRecipeInput inv) {
    if (inv != null && getModifiers(inv).stream().noneMatch(this.entryFilter)) {
      return NO_MATCHES;
    }
    return description;
  }

  @Override
  public List<ModifierEntry> getModifierOptions(@Nullable TinkerableRecipeInput inv) {
    if (inv == null) {
      if (filteredModifiers == null) {
        filteredModifiers = ModifierRecipeLookup.getRecipeModifierList().stream().filter(this.entryFilter).toList();
      }
      return filteredModifiers;
    }
    ToolStackView tool = inv.getTinkerable();
    SetComponent<ModifierId> existing = SetComponent.get(tool, component);
    Predicate<ModifierEntry> applicable = entry -> existing.contains(entry.id()) != addToSet;
    return getModifiers(inv).stream().filter(this.entryFilter).filter(applicable).toList();
  }

  @Override
  public RecipeResult<LazyToolStack> getResult(TinkerableRecipeInput inv, ModifierEntry modifier) {
    ToolStack tool = inv.getTinkerable().copy();
    // update the modifier set
    ModifierId id = modifier.id();
    if (addToSet) {
      SetComponent.add(tool, component, id);
    } else {
      SetComponent.remove(tool, component, id);
    }
    return LazyToolStack.success(tool);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.modifierSetWorktable.get();
  }
}
