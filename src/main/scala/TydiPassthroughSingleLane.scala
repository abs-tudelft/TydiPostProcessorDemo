package nl.tudelft.post_processor

import chisel3._
import chisel3.util.DecoupledIO
import nl.tudelft.tydi_chisel.{PhysicalStream, TydiEl, TydiModule}

/**
 * Passthrough module that converts between AXI and Tydi streams and back without changing the data.
 * @param ioType The type of the Tydi stream.
 * @param d The dimensionality of the Tydi-stream.
 * @param width The width of the AXI-stream.
 * @tparam T The type of the Tydi stream.
 */
class TydiPassthroughSingleLane[T <: TydiEl](ioType: T, d: Int, width: Width) extends TydiModule {
  val out: DecoupledIO[UInt] = IO(DecoupledIO(UInt(width)))
  val in: DecoupledIO[UInt] = IO(Flipped(DecoupledIO(UInt(width))))

  private val interStream = Wire(PhysicalStream(ioType, 1, d, 1))

  private val inConverter = Module(new Axi2TydiSingleLane(width, interStream))
  private val outConverter = Module(new Tydi2AxiSingleLane(width, interStream))

  inConverter.input <> in
  interStream := inConverter.output
  outConverter.input := interStream
  outConverter.output <> out
}
