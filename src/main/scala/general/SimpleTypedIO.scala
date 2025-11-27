package nl.tudelft.post_processor
package general

import chisel3._

trait SimpleTypedIO[A <: Bundle, B <: Bundle] extends Module {
  val in: A
  val out: B
}
