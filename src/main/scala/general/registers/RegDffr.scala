package timicasto.quantumcore
package general.registers

import chisel3._

class RegDffr(width: Int) extends Module {
	val io = IO(new Bundle() {
		val dnxt: UInt = Input(UInt(width.W))
		val qout: UInt = Output(UInt(width.W))
	})
	
	val outReg: UInt = Reg(UInt(width.W))
	
	when(!this.reset.asBool) {
		outReg := 0.U
	}.otherwise {
		outReg := this.io.dnxt
	}
	
	io.qout := outReg
}
