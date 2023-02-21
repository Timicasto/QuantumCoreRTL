package timicasto.quantumcore
package core.pipeline

import chisel3._

class IFReducedDecoder extends Module {
	override val compileOptions: CompileOptions = ExplicitCompileOptions.NotStrict.copy(explicitInvalidate = false)
	
	val io = IO(new Bundle() {
		val inInstruction: UInt = Input(UInt(32.W))
		
		val isLongIns: UInt = Output(UInt(1.W))
		val isJmp: UInt = Output(UInt(1.W))
		val isJal: UInt = Output(UInt(1.W))
		val isJalr: UInt = Output(UInt(1.W))
		val isConditionedJmp: UInt = Output(UInt(1.W))
		val isMulhsu: UInt = Output(UInt(1.W))
		val isMul: UInt = Output(UInt(1.W))
		val isDiv: UInt = Output(UInt(1.W))
		val isRem: UInt = Output(UInt(1.W))
		val isDivU: UInt = Output(UInt(1.W))
		val isRemU: UInt = Output(UInt(1.W))
		
		val hasRS1: UInt = Output(UInt(1.W))
		val hasRS2: UInt = Output(UInt(1.W))
		
		val valRS1: UInt = Output(UInt(5.W))
		val valRS2: UInt = Output(UInt(5.W))
		val valJalrRS1: UInt = Output(UInt(5.W))
		val ValBjpImm: UInt = Output(UInt(32.W))
	})
	
	val isLongIns: Bool = (((~(io.inInstruction.apply(4, 2) === 7.U)).asUInt) & ((io.inInstruction.apply(1, 0) === 3.U).asUInt)).asBool
	io.isLongIns := this.isLongIns
	
	private val isLongJal: Bool = ((io.inInstruction.apply(6, 5) === 3.U).asUInt & (io.inInstruction.apply(4, 2) === 3.U).asUInt & (io.inInstruction.apply(1, 0) === 3.U).asUInt).asBool
	private val isCompressedJal: Bool = ((io.inInstruction.apply(1, 0) === 1.U).asUInt & (io.inInstruction.apply(15, 13) === 1.U).asUInt).asBool
	private val isCompressedJ: Bool = ((io.inInstruction.apply(1, 0) === 1.U).asUInt & (io.inInstruction.apply(15, 13) === 5.U)).asBool
	
	io.isJal := isLongJal | isCompressedJal | isCompressedJ
	
	private val isCompressedJalrMask = (io.inInstruction.apply(1, 0) === 2.U).asUInt & (io.inInstruction.apply(15, 13) === 4.U).asUInt
	
	private val isLongJalr: Bool = ((io.inInstruction.apply(6, 5) === 3.U).asUInt & (io.inInstruction.apply(4, 2) === 1.U).asUInt & (io.inInstruction.apply(1, 0) === 3.U).asUInt).asBool
	private val isCompressedJr: Bool = (isCompressedJalrMask & (~(io.inInstruction.apply(12, 12))).asUInt & (~(io.inInstruction.apply(11, 7) === 0.U)).asUInt & (io.inInstruction.apply(6, 2) === 0.U)).asBool
	private val isCompressedJalr: Bool = (isCompressedJalrMask & (io.inInstruction.apply(12, 12)).asUInt & (~(io.inInstruction.apply(11, 7) === 0.U)).asUInt & (io.inInstruction.apply(6, 2) === 0.U)).asBool
	
	io.isJalr := isLongJalr | isCompressedJalr | isCompressedJr
	
	io.isJmp := io.isJal | io.isJalr | io.isConditionedJmp
}
