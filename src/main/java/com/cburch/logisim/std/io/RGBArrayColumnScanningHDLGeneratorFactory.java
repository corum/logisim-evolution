/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.io;

import com.cburch.logisim.util.LineBuffer;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.fpga.designrulecheck.Netlist;
import com.cburch.logisim.fpga.hdlgenerator.HDL;
import com.cburch.logisim.fpga.hdlgenerator.TickComponentHDLGeneratorFactory;

public class RGBArrayColumnScanningHDLGeneratorFactory extends LedArrayColumnScanningHDLGeneratorFactory {

  public static final String HDL_IDENTIFIER =  "RGBArrayColumnScanning";
  
  public RGBArrayColumnScanningHDLGeneratorFactory() {
    super();
    myWires
        .addWire("s_maxRedLedInputs", MAX_NR_LEDS_ID)
        .addWire("s_maxBlueLedInputs", MAX_NR_LEDS_ID)
        .addWire("s_maxGreenLedInputs", MAX_NR_LEDS_ID);
  }

  public static ArrayList<String> getPortMap(int id) {
    final var contents =
        (new LineBuffer())
            .pair("addr", LedArrayGenericHDLGeneratorFactory.LedArrayColumnAddress)
            .pair("clock", TickComponentHDLGeneratorFactory.FPGA_CLOCK)
            .pair("insR", LedArrayGenericHDLGeneratorFactory.LedArrayRedInputs)
            .pair("insG", LedArrayGenericHDLGeneratorFactory.LedArrayGreenInputs)
            .pair("insB", LedArrayGenericHDLGeneratorFactory.LedArrayBlueInputs)
            .pair("outsR", LedArrayGenericHDLGeneratorFactory.LedArrayRowRedOutputs)
            .pair("outsG", LedArrayGenericHDLGeneratorFactory.LedArrayRowGreenOutputs)
            .pair("outsB", LedArrayGenericHDLGeneratorFactory.LedArrayRowBlueOutputs)
            .pair("id", id);

    if (HDL.isVHDL()) {
      contents.add("""
          PORT MAP ( {{addr }} => {{addr}}{{id}},
                     {{clock}} => {{clock}},
                     {{outsR}} => {{outsR}}{{id}},
                     {{outsG}} => {{outsG}}{{id}},
                     {{outsB}} => {{outsB}}{{id}},
                     {{insR }} => s_{{insR}}{{id}},
                     {{insG }} => s_{{insG}}{{id}},
                     {{insB }} => s_{{insB}}{{id}} );
          """);
    } else {
      contents.add("""
          ( .{{addr }}({{addr}}{{id}}),
            .{{clock}}({{clock}}),
            .{{outsR}}({{outsR}}{{id}}),
            .{{outsG}}({{outsG}}{{id}}),
            .{{outsB}}({{outsB}}{{id}}),
            .{{insR }}(s_{{insR}}{{id}}),
            .{{insG }}(s_{{insG}}{{id}}),
            .{{insB }}(s_{{insB}}{{id}}) );
          """);
    }
    return contents.getWithIndent(6);
  }

  @Override
  public SortedMap<String, Integer> GetOutputList(Netlist TheNetlist, AttributeSet attrs) {
    final var outputs = new TreeMap<String, Integer>();
    outputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayColumnAddress, NR_OF_COLUMN_ADDRESS_BITS_ID);
    outputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayRowRedOutputs, NR_OF_ROWS_ID);
    outputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayRowGreenOutputs, NR_OF_ROWS_ID);
    outputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayRowBlueOutputs, NR_OF_ROWS_ID);
    return outputs;
  }

  @Override
  public SortedMap<String, Integer> GetInputList(Netlist TheNetlist, AttributeSet attrs) {
    final var inputs = new TreeMap<String, Integer>();
    inputs.put(TickComponentHDLGeneratorFactory.FPGA_CLOCK, 1);
    inputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayRedInputs, NR_OF_LEDS_ID);
    inputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayGreenInputs, NR_OF_LEDS_ID);
    inputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayBlueInputs, NR_OF_LEDS_ID);
    return inputs;
  }

  @Override
  public ArrayList<String> GetModuleFunctionality(Netlist netlist, AttributeSet attrs) {
    final var contents =
        (new LineBuffer())
            .pair("nrOfLeds", NR_OF_LEDS_STRING)
            .pair("nrOfRows", NR_OF_ROWS_STRING)
            .pair("activeLow", ACTIVE_LOW_STRING)
            .pair("insR", LedArrayGenericHDLGeneratorFactory.LedArrayRedInputs)
            .pair("insG", LedArrayGenericHDLGeneratorFactory.LedArrayGreenInputs)
            .pair("insB", LedArrayGenericHDLGeneratorFactory.LedArrayBlueInputs)
            .pair("outsR", LedArrayGenericHDLGeneratorFactory.LedArrayRowRedOutputs)
            .pair("outsG", LedArrayGenericHDLGeneratorFactory.LedArrayRowGreenOutputs)
            .pair("outsB", LedArrayGenericHDLGeneratorFactory.LedArrayRowBlueOutputs);

    contents.add(getColumnCounterCode());
    if (HDL.isVHDL()) {
      contents.add("""
          makeVirtualInputs : PROCESS ( internalRedLeds, internalGreenLeds, internalBlueLeds ) IS
          BEGIN
             s_maxRedLedInputs <= (OTHERS => '0');
             s_maxGreenLedInputs <= (OTHERS => '0');
             s_maxBlueLedInputs <= (OTHERS => '0');
             IF ({{activeLow}} = 1) THEN
                s_maxRedLedInputs({{nrOfLeds}}-1 DOWNTO 0)   <= NOT {{insR}};
                s_maxGreenLedInputs({{nrOfLeds}}-1 DOWNTO 0) <= NOT {{insG}};
                s_maxBlueLedInputs({{nrOfLeds}}-1 DOWNTO 0)  <= NOT {{insB}};
             ELSE
                s_maxRedLedInputs({{nrOfLeds}}-1 DOWNTO 0)   <= {{insR}};
                s_maxGreenLedInputs({{nrOfLeds}}-1 DOWNTO 0) <= {{insG}};
                s_maxBlueLedInputs({{nrOfLeds}}-1 DOWNTO 0)  <= {{insB}};
             END IF;
          END PROCESS makeVirtualInputs;
          
          GenOutputs : FOR n IN {{nrOfRows}}-1 DOWNTO 0 GENERATE
             {{outsR}}(n) <= s_maxRedLedInputs(to_integer(unsigned(s_columnCounterReg)) + n*nrOfColumns);
             {{outsG}}(n) <= s_maxGreenLedInputs(to_integer(unsigned(s_columnCounterReg)) + n*nrOfColumns);
             {{outsB}}(n) <= s_maxBlueLedInputs(to_integer(unsigned(s_columnCounterReg)) + n*nrOfColumns);
          END GENERATE GenOutputs;
          """);
    } else {
      contents.add("""

          genvar i;
          generate
             for (i = 0; i < {{nrOfRows}}; i = i + 1)
             begin:outputs
                assign {{outsR}}[i] = (activeLow == 1)
                    ? ~{{insR }}[i*nrOfColumns+s_columnCounterReg]
                    :  {{insR }}[i*nrOfColumns+s_columnCounterReg];
                assign {{outsG}}[i] = (activeLow == 1)
                    ? ~{{insG }}[i*nrOfColumns+s_columnCounterReg]
                    :  {{insG }}[i*nrOfColumns+s_columnCounterReg];
                assign {{outsB}}[i] = (activeLow == 1)
                    ? ~{{insB }}[i*nrOfColumns+s_columnCounterReg]
                    :  {{insB }}[i*nrOfColumns+s_columnCounterReg];
             end
          endgenerate
          """);
    }
    return contents.getWithIndent();
  }
}
