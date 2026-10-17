package slimeknights.tconstruct.library.tools.definition;

import com.google.common.collect.ImmutableMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;

import java.util.Map;
import java.util.Map.Entry;

/** Packet sending tool definition data from server to client. */
@Internal
public record UpdateToolDefinitionDataPacket(Map<ResourceLocation, ToolDefinitionData> dataMap) implements ISimplePacket {
  public static final Type<UpdateToolDefinitionDataPacket> TYPE = new Type<>(TConstruct.getResource("update_tool_definitions"));
  public static final StreamCodec<RegistryFriendlyByteBuf,UpdateToolDefinitionDataPacket> CODEC = new StreamCodec<>() {
    @Override
    public UpdateToolDefinitionDataPacket decode(RegistryFriendlyByteBuf buffer) {
      int size = buffer.readVarInt();
      ImmutableMap.Builder<ResourceLocation, ToolDefinitionData> builder = ImmutableMap.builder();
      for (int i = 0; i < size; i++) {
        ResourceLocation name = buffer.readResourceLocation();
        try {
          ToolDefinitionData data = ToolDefinitionData.LOADABLE.decode(buffer, ToolDefinitionLoader.contextBuilder(name).build());
          builder.put(name, data);
        } catch (RuntimeException e) {
          TConstruct.LOG.error("Failed to decode Tool Definition for {}", name, e);
          throw e;
        }
      }
      return new UpdateToolDefinitionDataPacket(builder.build());
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, UpdateToolDefinitionDataPacket packet) {
      buffer.writeVarInt(packet.dataMap.size());
      for (Entry<ResourceLocation, ToolDefinitionData> entry : packet.dataMap.entrySet()) {
        ResourceLocation name = entry.getKey();
        buffer.writeResourceLocation(name);
        try {
          ToolDefinitionData.LOADABLE.encode(buffer, entry.getValue());
        } catch (RuntimeException e) {
          TConstruct.LOG.error("Failed to encode Tool Definition for {}", name, e);
          throw e;
        }
      }
    }
  };

  @Override
  public Type<UpdateToolDefinitionDataPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    ToolDefinitionLoader.getInstance().updateDataFromServer(dataMap);
  }
}
