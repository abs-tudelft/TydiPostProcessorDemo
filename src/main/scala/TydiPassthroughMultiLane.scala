package nl.tudelft.post_processor

import chisel3._
import chisel3.util.DecoupledIO
import nl.tudelft.tydi_chisel.{PhysicalStream, TydiEl, TydiModule}

/**
 * Passthrough module that converts between AXI and Tydi streams and back without changing the data.
 * @param ioType The type of the Tydi stream.
 * @param d The dimensionality of the Tydi-stream.
 * @param n The number of parallel packet lanes.
 * @param width The width of the AXI-stream.
 * @tparam T The type of the Tydi stream.
 */
class TydiPassthroughMultiLane[T <: TydiEl](ioType: T, d: Int, n: Int, width: Int) extends TydiModule with SimpleTypedIO[DecoupledIO[UInt], DecoupledIO[UInt]] {
  private val axiWidth = width * n
  val out: DecoupledIO[UInt] = IO(DecoupledIO(UInt(axiWidth.W)))
  val in: DecoupledIO[UInt] = IO(Flipped(DecoupledIO(UInt(axiWidth.W))))

  private val interStream = Wire(PhysicalStream(ioType, n, d, 8))

  private val inConverter = Module(new Axi2TydiMultiLane(width, interStream))
  private val outConverter = Module(new Tydi2AxiMultiLane(width, interStream))

  inConverter.input <> in
  interStream := inConverter.output
  outConverter.input := interStream
  outConverter.output <> out
}
