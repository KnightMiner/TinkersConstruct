package slimeknights.tconstruct.tools.network;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.mantle.network.codec.MantleStreamCodecs;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.tools.logic.InteractionHandler;

/** Packet sent client to server when an empty hand interaction */
@RequiredArgsConstructor
public enum InteractWithAirPacket implements ISimplePacket {
  /** Right click with an empty main hand and a chestplate */
  MAINHAND(InteractionHand.MAIN_HAND),
  /** Right click with an empty off hand and a chestplate */
  OFFHAND(InteractionHand.OFF_HAND),
  /** Left click with a supported tool */
  LEFT_CLICK(InteractionHand.MAIN_HAND);

  public static final Type<InteractWithAirPacket> TYPE = new Type<>(TConstruct.getResource("interact_with_air"));
  public static final StreamCodec<ByteBuf,InteractWithAirPacket> CODEC = MantleStreamCodecs.enumCodec(InteractWithAirPacket.class, OutOfBoundsStrategy.WRAP);

  private final InteractionHand hand;

  /** Gets the packet for the given hand */
  public static InteractWithAirPacket fromChestplate(InteractionHand hand) {
    return hand == InteractionHand.OFF_HAND ? OFFHAND : MAINHAND;
  }

  @Override
  public Type<InteractWithAirPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    Player player = context.player();
    if (!player.isSpectator()) {
      if (this == LEFT_CLICK) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(TinkerTags.Items.INTERACTABLE_LEFT)) {
          InteractionResult result = InteractionHandler.onLeftClickInteraction(player, held, hand);
          if (result.shouldSwing()) {
            player.swing(hand, true);
          }
        }
      } else {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.is(TinkerTags.Items.INTERACTABLE_ARMOR) && player.getItemInHand(hand).isEmpty()) {
          InteractionResult result = InteractionHandler.onChestplateUse(player, chestplate, hand);
          if (result.shouldSwing()) {
            player.swing(hand, true);
          }
        }
      }
    }
  }
}
