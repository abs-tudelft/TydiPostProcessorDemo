package nl.tudelft.post_processor
package active

import chisel3._
import nl.tudelft.tydi_chisel._

class WordCounter(n: Int, d: Int) extends SubProcessorBase(new BitsEl(8.W), new BitsEl(1.W), dIn = d, dOut = d, nIn = n, nOut = n) {
  private val counters = Seq.fill(n)(new WordCountElement)

  // Feed characters into the counter components.
  counters.zip(inStream.data).foreach { case (counter, char) =>
    counter.char := char
  }

  // Connect the counters front-to-back.
  private val prevIsWordPart = RegNext(counters.last.isWordPart, false.B)
  counters.head.isWordPart := prevIsWordPart
  counters.zip(counters.tail).foreach { case (counter, prevCounter) =>
    counter.prevIsWordPart := prevCounter.isWordPart
  }

  // For non-enabled lanes, we make sure that the counter passes through the previous state.
  private val enabledLanes = inStream.laneValidityVec
  counters.zip(enabledLanes).foreach { case (counter, enabled) =>
    counter.enabled := enabled
  }

  // When an element closes off a lane, we make sure the next element starts clean.
  counters.zip(inStream.last).foreach { case (counter, last) =>
    counter.forceClose := last.orR
  }

  counters.zip(outStream.data).foreach { case (counter, outLane) =>
    outLane := counter.startsWord
  }
}
