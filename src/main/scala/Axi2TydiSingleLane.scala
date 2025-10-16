package nl.tudelft.post_processor

import chisel3._
import chisel3.util._
import nl.tudelft.tydi_chisel.{PhysicalStream, TydiModule}

class Axi2TydiSingleLane(sourceWidth: Width, sink: PhysicalStream) extends TydiModule {
  val input: DecoupledIO[UInt] = IO(Flipped(DecoupledIO(UInt(sourceWidth))))
  val output: PhysicalStream = IO(sink.cloneType)

  input.ready := output.ready
  output.valid := input.valid
  output.strb := input.bits(0)
  output.last := input.bits(sink.d, 1)
  output.data := input.bits(sink.getWidth + sink.d, sink.d + 1)
  output.user := DontCare
  output.stai := 0.U
  output.endi := 0.U
}
