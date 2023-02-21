package timicasto.quantumcore

import chisel3._
import chisel3.experimental._
import timicasto.quantumcore.core.pipeline.IFReducedDecoder
import timicasto.quantumcore.general.registers.{RegDffl, RegDfflr, RegDfflrs, RegLatch}

object main extends App {
  println(getVerilogString(new IFReducedDecoder))
}