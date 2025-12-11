package nl.tudelft.post_processor
package active

import chisel3._

class WordCountElement extends Module {
  val char: UInt = IO(Input(UInt(8.W)))
  val prevIsWordPart: Bool = IO(Input(Bool()))
  val enabled: Bool = IO(Input(Bool()))
  val forceClose: Bool = IO(Input(Bool()))
  val isWordPart: Bool = IO(Output(Bool()))
  val startsWord: Bool = IO(Output(Bool()))

  private val isLower = char >= 97.U && char <= 122.U
  private val isUpper = char >= 65.U && char <= 90.U
  private val isDigit = char >= 48.U && char <= 57.U
  private val isOtherWordPart = char === 45.U || char === 95.U || char === 39.U // -_'

  when (enabled) {
    isWordPart := (isLower || isUpper || isDigit || isOtherWordPart) && !forceClose
    startsWord := !prevIsWordPart && isWordPart
  } otherwise {
    isWordPart := prevIsWordPart
    startsWord := false.B
  }
}
