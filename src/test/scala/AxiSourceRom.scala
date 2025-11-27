package nl.tudelft.post_processor

import TydiPayloadKit.TydiBinaryStream
import chisel3._
import chisel3.util.{Counter, DecoupledIO}

/**
 * A simple ROM that outputs the elements of a TydiBinaryStream in order.
 * @param elements The elements to output.
 * @param width The width of the AXI lane.
 * @param streamName The name of the stream. Just for organizational purposes.
 */
class AxiSourceRom(elements: TydiBinaryStream, width: Width, val streamName: String) extends Module {
  private val literals: Seq[UInt] = elements.map(_.data.U(width))
  private val rom: Vec[UInt] = VecInit(literals)
  private val counter = Counter(rom.length)

  val output: DecoupledIO[UInt] = IO(DecoupledIO(UInt(width)))

  output.bits := rom(counter.value)

  output.valid := counter.value < rom.length.U

  when (output.ready) {
    counter.inc()
  }
}
