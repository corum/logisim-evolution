/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.ttl;

import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.util.LineBuffer;

/**
 * TTL 74x7266: quad 2-input XNOR gate
 * Model based on https://www.ti.com/product/CD74HC7266 datasheet.
 */
public class Ttl747266 extends AbstractTtlGate {
  /**
   * Unique identifier of the tool, used as reference in project files.
   * Do NOT change as it will prevent project files from loading.
   *
   * Identifier value must MUST be unique string among all tools.
   */
  public static final String _ID = "747266";

  private static class XNorGateHdlGeneratorFactory extends AbstractGateHdlGenerator {

    @Override
    public LineBuffer getLogicFunction(int index) {
      return LineBuffer.getHdlBuffer()
          .add("{{assign}}gateO{{1}}{{=}}{{not}}(gateA{{1}}{{xor}}gateB{{1}});", index);
    }
  }

  public Ttl747266() {
    super(_ID, (byte) 14, new byte[] {3, 6, 8, 11}, true, new XNorGateHdlGeneratorFactory());
  }

  @Override
  public void paintInternal(InstancePainter painter, int x, int y, int height, boolean up) {
    final var g = painter.getGraphics();
    final var portwidth = 18;
    final var portheight = 15;
    final var youtput = y + (up ? 20 : 40);
    Drawgates.paintXor(g, x + 44, youtput, portwidth, portheight, true);
    // output line
    Drawgates.paintOutputgate(g, x + 50, y, x + 48, youtput, up, height);
    // input lines
    Drawgates.paintDoubleInputgate(
        g, x + 30, y, x + 44 - portwidth, youtput, portheight, up, false, height);
  }

  @Override
  public void propagateTtl(InstanceState state) {
    for (byte i = 2; i < 6; i += 3) {
      state.setPort(i, (state.getPortValue(i - 1).xor(state.getPortValue(i - 2)).not()), 1);
    }
    for (byte i = 6; i < 12; i += 3) {
      state.setPort(i, (state.getPortValue(i + 1).xor(state.getPortValue(i + 2)).not()), 1);
    }
  }
}
