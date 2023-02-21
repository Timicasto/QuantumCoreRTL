package timicasto.quantumcore
package general.registers

import chisel3._

class RegLatch(width: Int) extends Module {
	val io = IO(new Bundle() {
		val lden: UInt = Input(UInt(1.W))
		val dnxt: UInt = Input(UInt(width.W))
		val qout: UInt = Output(UInt(width.W))
	})
	
	val outReg: UInt = Reg(UInt(width.W))
	
	io.qout := outReg
	
	when (io.lden.asBool) {
		outReg := io.dnxt
	}
}
