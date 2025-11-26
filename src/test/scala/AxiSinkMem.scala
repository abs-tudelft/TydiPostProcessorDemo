package nl.tudelft.post_processor

import TydiPayloadKit.TydiBinaryStream
import chisel3._
import chisel3.util.{Counter, DecoupledIO}

/**
 * A simple memory that stores the last N elements of a TydiBinaryStream.
 * @param capacity The capacity of the memory in number of elements.
 * @param width The width of the AXI lane.
 */
class AxiSinkMem(capacity: Int, width: Width) extends Module {
  private val mem: Vec[UInt] = RegInit(VecInit.fill(capacity)(0.U(width)))
  private val counter = Counter(mem.length)

  val input: DecoupledIO[UInt] = IO(Flipped(DecoupledIO(UInt(width))))
  val data: Vec[UInt] = IO(Output(mem.cloneType))
  data := mem
  val length: UInt = IO(Output(counter.value.cloneType))
  length := counter.value

  input.ready := counter.value < mem.length.U

  when (input.valid) {
    counter.inc()
    mem(counter.value) := input.bits
  }
}
