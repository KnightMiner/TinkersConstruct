package slimeknights.tconstruct.smeltery.client.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

/**
 * Vertex builder wrapper that tints all quads passed in
 */
public class TintedVertexBuilder extends VertexConsumerWrapper {
  /** Tint color from 0-255 */
  private final int tintRed, tintGreen, tintBlue, tintAlpha;

  public TintedVertexBuilder(VertexConsumer parent, int tintRed, int tintGreen, int tintBlue, int tintAlpha) {
    super(parent);
    this.tintRed = tintRed;
    this.tintGreen = tintGreen;
    this.tintBlue = tintBlue;
    this.tintAlpha = tintAlpha;
  }

  @Override
  public VertexConsumer setColor(int red, int green, int blue, int alpha) {
    parent.setColor((red * tintRed) / 0xFF, (green * tintGreen) / 0xFF, (blue * tintBlue) / 0xFF, (alpha * tintAlpha) / 0xFF);
    return this;
  }
}
