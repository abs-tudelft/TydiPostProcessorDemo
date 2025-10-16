package nl.tudelft.post_processor

import chisel3._
import chisel3.util._
import nl.tudelft.tydi_chisel.{PhysicalStream, TydiModule}

class Tydi2AxiSingleLane(sinkWidth: Width, source: PhysicalStream) extends TydiModule {
  val input: PhysicalStream = IO(Flipped(source.cloneType))
  val output: DecoupledIO[UInt] = IO(DecoupledIO(UInt(sinkWidth)))

  input.ready := output.ready
  output.valid := input.valid
  output.bits := input.strb ## input.data ## input.data
}
