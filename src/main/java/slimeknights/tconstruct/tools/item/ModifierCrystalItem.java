package slimeknights.tconstruct.tools.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.mantle.command.MantleCommand;
import slimeknights.mantle.fluid.FluidTransferHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.TinkerTags.Modifiers;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.modifiers.hook.build.ModifierRemovalHook;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.Util;
import slimeknights.tconstruct.tools.TinkerModifiers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/** Dynamic item holding a modifier */
public class ModifierCrystalItem extends Item {
  private static final Component TOOLTIP_MISSING = TConstruct.makeTranslation("item", "modifier_crystal.missing").withStyle(ChatFormatting.GRAY);
  private static final Component TOOLTIP_APPLY = TConstruct.makeTranslation("item", "modifier_crystal.tooltip").withStyle(ChatFormatting.GRAY);
  private static final String MODIFIER_KEY = TConstruct.makeTranslationKey("item", "modifier_crystal.modifier_id");
  public ModifierCrystalItem(Properties props) {
    super(props);
  }

  @Override
  public boolean isFoil(ItemStack pStack) {
    return true;
  }

  @Override
  public Component getName(ItemStack stack) {
    ModifierHolder modifier = getModifier(stack);
    if (modifier.isPresent()) {
      Component modifierName;
      if (modifier.isBound()) {
        modifierName = modifier.get().getDisplayName();
      } else {
        modifierName = Component.translatable(Util.makeTranslationKey("modifier", modifier.id().id()));
      }
      return Component.translatable(getDescriptionId(stack) + ".format", modifierName).setStyle(modifierName.getStyle());
    }
    return super.getName(stack);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag advanced) {
    ModifierHolder modifier = getModifier(stack);
    if (modifier.isPresent()) {
      if (modifier.isBound()) {
        tooltip.addAll(modifier.get().getDescriptionList());
      }
      tooltip.add(TOOLTIP_APPLY);
      if (advanced.isAdvanced()) {
        tooltip.add((Component.translatable(MODIFIER_KEY, modifier.id().toString())).withStyle(ChatFormatting.DARK_GRAY));
      }
    } else {
      tooltip.add(TOOLTIP_MISSING);
    }
  }

  @Nullable
  @Override
  public String getCreatorModId(ItemStack stack) {
    ModifierHolder modifier = getModifier(stack);
    if (modifier.isPresent()) {
      return modifier.id().getNamespace();
    }
    return null;
  }

  /** @see slimeknights.tconstruct.shared.command.subcommand.ModifiersCommand */
  @Override
  public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
    // stacking a crystal on a tool attempts to add it when in creative
    // see also - modifier adding command

    // must be op or in creative, right-clicking onto a modifiable slot
    if (action == ClickAction.SECONDARY && slot.allowModification(player) && CreativeSlotItem.canApply(player)) {
      ModifierHolder modifier = getModifier(stack);
      ItemStack toolItem = slot.getItem();
      // slot must have a tool, NBT must be valid
      if (modifier.isBound() && !toolItem.isEmpty() && toolItem.is(TinkerTags.Items.MODIFIABLE)) {
        if (!player.level().isClientSide || (player.isCreative() && player.containerMenu.menuType == null)) {
          ToolStack tool = ToolStack.copyFrom(toolItem);

          // add modifier
          // TODO: this method should take a holder
          tool.addModifier(modifier.id(), stack.getCount());

          // ensure no modifier problems after adding
          Component toolValidation = tool.tryValidate();
          if (toolValidation != null) {
            player.displayClientMessage(toolValidation, false);
          } else {
            tool.updateStack(toolItem);
            FluidTransferHelper.playUISound(player, SoundEvents.ENCHANTMENT_TABLE_USE);
          }
        }
        return true;
      }
      return false;
    }
    return false;
  }

  /** @see slimeknights.tconstruct.shared.command.subcommand.ModifiersCommand */
  @Override
  public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack toolItem, Slot slot, ClickAction action, Player player, SlotAccess access) {
    // stacking a tool on a crystal attempts to remove the modifier in creative
    // see also - modifier removal command

    // must be op or in creative, right-clicking onto a modifiable slot with a tool
    if (action == ClickAction.SECONDARY && slot.allowModification(player) && !toolItem.isEmpty() && toolItem.is(TinkerTags.Items.MODIFIABLE) && (player.isCreative() || player.hasPermissions(MantleCommand.PERMISSION_GAME_COMMANDS))) {
      // NBT must be valid
      ModifierHolder modifier = getModifier(stack);
      if (modifier.isBound()) {
        if (!player.level().isClientSide || (player.isCreative() && player.containerMenu.menuType == null)) {
          ToolStack original = ToolStack.from(toolItem);
          ToolStack tool = original.copy();

          // ensure we have something to remove
          ModifierEntry entry = tool.getUpgrades().getEntry(modifier.id());
          if (entry.getLevel() <= 0) {
            return true;
          }
          // call remove hook
          int newLevel = entry.getLevel() - stack.getCount();
          if (newLevel <= 0) {
            entry.getHook(ModifierHooks.RAW_DATA).removeRawData(tool, entry.getModifier(), tool.getRestrictedNBT());
          }
          tool.removeModifier(modifier.id(), stack.getCount());

          // ensure no modifier problems after adding
          Component toolValidation = tool.tryValidate();
          if (toolValidation != null) {
            player.displayClientMessage(toolValidation, false);
            return true;
          }

          // ask modifiers if it's okay to remove them
          toolValidation = ModifierRemovalHook.onRemoved(original, tool);
          if (toolValidation != null) {
            player.displayClientMessage(toolValidation, false);
            return true;
          }

          // success, update tool
          tool.updateStack(toolItem);
          FluidTransferHelper.playUISound(player, SoundEvents.GRINDSTONE_USE);
        }
        return true;
      }
      return false;
    }
    return false;
  }


  /* Helpers */

  /** Creates a stack with the given modifier */
  public static ItemStack withModifier(ModifierId modifier, int count) {
    ItemStack stack = new ItemStack(TinkerModifiers.modifierCrystal.get(), count);
    stack.set(TinkerData.MODIFIER, ModifierHolder.create(modifier));
    return stack;
  }

  /** Creates a stack with the given modifier */
  public static ItemStack withModifier(ModifierId modifier) {
    return withModifier(modifier, 1);
  }

  /** Gets the modifier stored on this stack */
  public static ModifierHolder getModifier(ItemStack stack) {
    return stack.getOrDefault(TinkerData.MODIFIER, ModifierHolder.EMPTY);
  }

  /** Gets all variants of this item */
  public static void addVariants(Consumer<ItemStack> items) {
    ModifierRecipeLookup.getRecipeModifierList().forEach(modifier -> {
      if (!ModifierManager.isInTag(modifier.id(), Modifiers.EXTRACT_MODIFIER_BLACKLIST)) {
        items.accept(withModifier(modifier.id()));
      }
    });
  }
}
