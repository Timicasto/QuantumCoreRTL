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
		val valBjpImm: UInt = Output(UInt(32.W))
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
	
	private val isLongBranch: Bool = ((io.inInstruction.apply(6, 5) === 3.U).asUInt & (io.inInstruction.apply(4, 2) === 0.U).asUInt & (io.inInstruction.apply(1, 0) === 3.U).asUInt).asBool
	private val isCompressedBeqz: Bool = ((io.inInstruction.apply(1, 0) === 1.U).asUInt & (io.inInstruction.apply(15, 13) === 6.U).asUInt).asBool
	private val isCompressedBnez: Bool = ((io.inInstruction.apply(1, 0) === 1.U).asUInt & (io.inInstruction.apply(15, 13) === 7.U).asUInt).asBool
	
	io.isConditionedJmp := isLongBranch | isCompressedBeqz | isCompressedBnez
	
	private val isLongOp: Bool = ((io.inInstruction.apply(6, 5) === 1.U).asUInt & (io.inInstruction.apply(4, 2) === 4.U).asUInt & (io.inInstruction.apply(1, 0) === 3.U)).asBool
	
	// rv32 func3 14:12 rv32 func7 31:25 rv32 rd 11:7 rv32 rs1 rs2 19:15 24:20
	private val isLongMulh: Bool = ((isLongOp.asUInt) & (io.inInstruction.apply(14, 12) === 1.U).asUInt & (io.inInstruction.apply(31, 25) === 1.U).asUInt).asBool
	private val isLongMulhsu: Bool = ((isLongOp.asUInt) & (io.inInstruction.apply(14, 12) === 2.U).asUInt & (io.inInstruction.apply(31, 25) === 1.U).asUInt).asBool
	private val isLongMulhu: Bool = ((isLongOp.asUInt) & (io.inInstruction.apply(14, 12) === 3.U).asUInt & (io.inInstruction.apply(31, 25) === 1.U).asUInt).asBool
	
	io.isMulhsu := isLongMulh | isLongMulhu | isLongMulhsu
	
	io.isMul := ((isLongOp.asUInt) & (io.inInstruction.apply(14, 12) === 0.U).asUInt & (io.inInstruction.apply(31, 25) === 1.U).asUInt)
	io.isDiv := ((isLongOp.asUInt) & (io.inInstruction.apply(14, 12) === 4.U).asUInt & (io.inInstruction.apply(31, 25) === 1.U).asUInt)
	io.isDivU := ((isLongOp.asUInt) & (io.inInstruction.apply(14, 12) === 5.U).asUInt & (io.inInstruction.apply(31, 25) === 1.U).asUInt)
	io.isRem := ((isLongOp.asUInt) & (io.inInstruction.apply(14, 12) === 6.U).asUInt & (io.inInstruction.apply(31, 25) === 1.U).asUInt)
	io.isRemU := ((isLongOp.asUInt) & (io.inInstruction.apply(14, 12) === 7.U).asUInt & (io.inInstruction.apply(31, 25) === 1.U).asUInt)
	
	// TODO: Compressed instruction RS value decoding
	io.valRS1 := (io.inInstruction.apply(19, 15))
	io.valRS2 := (io.inInstruction.apply(24, 20))
	
	// TODO: RS Enable signal decoding
	
	io.valJalrRS1 := Mux(isLongIns, io.inInstruction.apply(19, 15), io.inInstruction.apply(11, 7))
	// TODO: BJP instruction immediate number decoding
	
	io.isJmp := io.isJal | io.isJalr | io.isConditionedJmp
}
