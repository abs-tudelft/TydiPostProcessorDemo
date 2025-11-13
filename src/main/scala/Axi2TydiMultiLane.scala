package nl.tudelft.post_processor

import chisel3._
import chisel3.util._
import nl.tudelft.tydi_chisel.{PhysicalStream, TydiModule}

class Axi2TydiMultiLane(sourceWidth: Int, sink: PhysicalStream) extends TydiModule {
  private val n = sink.n
  private val axiWidth = sourceWidth * n
  private val d = sink.d
  val input: DecoupledIO[UInt] = IO(Flipped(DecoupledIO(UInt(axiWidth.W))))
  val output: PhysicalStream = IO(sink.cloneType)

  private val inputVec = WireInit(
    VecInit.tabulate(n) { i => input.bits((i+1) * sourceWidth - 1, i * sourceWidth) }
  )
  private val outputDataVec = Wire(Vec(n, UInt(output.elWidth.W)))
  output.data := outputDataVec.asUInt
  private val outputLastVec = Wire(Vec(n, UInt(d.W)))
  output.last := outputLastVec.asUInt
  private val outputStrbVec = Wire(Vec(n, Bool()))
  output.strb := outputStrbVec.asUInt

  input.ready := output.ready
  output.valid := input.valid

  inputVec.lazyZip(outputDataVec).lazyZip(outputLastVec).lazyZip(outputStrbVec).foreach { case (in, outData, outLast, outStrb) =>
    outStrb := in(0)
    outLast := in(sink.d, 1)
    outData := in(sink.elWidth + sink.d, sink.d + 1)
  }

  output.user := DontCare
  output.stai := 0.U
  output.endi := (n-1).U
}
