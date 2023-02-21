package timicasto.quantumcore
package general.registers

import chisel3._

class RegDffrs(width: Int) extends Module {
	val io = IO(new Bundle() {
		val dnxt: UInt = Input(UInt(width.W))
		val qout: UInt = Output(UInt(width.W))
	})
	
	val outReg: UInt = Reg(UInt(width.W))
	
	when(!this.reset.asBool) {
		outReg := (Math.pow(2, width) - 1).toLong.asUInt
	}.otherwise {
		outReg := this.io.dnxt
	}
	
	io.qout := outReg
}
