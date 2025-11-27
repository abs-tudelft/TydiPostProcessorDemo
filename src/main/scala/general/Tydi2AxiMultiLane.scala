package nl.tudelft.post_processor
package general

import chisel3._
import chisel3.util._
import nl.tudelft.tydi_chisel.{PhysicalStream, TydiModule}

class Tydi2AxiMultiLane(sinkWidth: Int, source: PhysicalStream) extends TydiModule {
  private val n = source.n
  private val axiWidth = sinkWidth * n
  private val d = source.d
  val input: PhysicalStream = IO(Flipped(source.cloneType))
  val output: DecoupledIO[UInt] = IO(DecoupledIO(UInt(axiWidth.W)))

  private val elWidth = input.elWidth
  private val inputDataVec = WireInit(
    VecInit.tabulate(n) { i => input.data((i+1) * elWidth - 1, i * elWidth) }
  )
  private val inputLastVec = WireInit(
    VecInit.tabulate(n) { i => input.last((i+1) * d - 1, i * d) }
  )
  private val inputStrbVec = WireInit(
    VecInit.tabulate(n) { i => input.strb(i) }
  )
  private val outputVec = Wire(Vec(n, UInt(sinkWidth.W)))
  output.bits := outputVec.asUInt

  inputDataVec.lazyZip(inputLastVec).lazyZip(inputStrbVec).zip(outputVec).foreach { case ((data, last, strb), out) =>
    out := Cat(data, last, strb)
  }

  input.ready := output.ready
  output.valid := input.valid
  input.user := DontCare
}
