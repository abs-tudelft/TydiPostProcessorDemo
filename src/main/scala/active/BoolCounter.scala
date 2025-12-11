package nl.tudelft.post_processor
package active

import chisel3._
import chisel3.util.PopCount
import nl.tudelft.tydi_chisel._

// This component is compatible until C=7 because multiple sequence closings per transfer are not supported. Then we would need to also have n output lanes.
class BoolCounter(n: Int, d: Int, outSize: Width) extends SubProcessorBase(new BitsEl(1.W), new BitsEl(outSize), dIn = d, dOut = d, nIn = n, nOut = 1, cIn = 7, cOut = 7) {
  private val counter = RegInit(0.U(outSize))
  private val countInTransfer = PopCount(inStream.data.asUInt & inStream.laneValidity)
  counter := counter + countInTransfer
  outStream.data := counter

  private val sequenceClosing = inStream.last.asUInt.orR
  outStream.valid := sequenceClosing
  when (sequenceClosing) {
    counter := 0.U
  }
}
