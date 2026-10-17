package slimeknights.tconstruct.shared.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.mantle.network.codec.MantleStreamCodecs;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.shared.client.ClientGeneratePartTexturesCommand;

/**
 * Packet to tell the client to generate tool textures
 * FIXME: this should just be a client command.
 */
@Internal
public record GeneratePartTexturesPacket(Operation operation, String modId, String materialPath) implements ISimplePacket {
  public static final Type<GeneratePartTexturesPacket> TYPE = new Type<>(TConstruct.getResource("generate_part_textures"));
  public static final StreamCodec<ByteBuf,GeneratePartTexturesPacket> CODEC = StreamCodec.composite(
    MantleStreamCodecs.enumCodec(Operation.class, OutOfBoundsStrategy.ZERO), GeneratePartTexturesPacket::operation,
    ByteBufCodecs.STRING_UTF8, GeneratePartTexturesPacket::modId,
    ByteBufCodecs.STRING_UTF8, GeneratePartTexturesPacket::materialPath,
    GeneratePartTexturesPacket::new);

  @Override
  public Type<GeneratePartTexturesPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    ClientGeneratePartTexturesCommand.generateTextures(operation, modId, materialPath);
  }

  public enum Operation { ALL, MISSING }
}
