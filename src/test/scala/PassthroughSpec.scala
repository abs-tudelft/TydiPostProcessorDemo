package nl.tudelft.post_processor

import chisel3._
import chisel3.util.DecoupledIO
import chiseltest.{ChiselScalatestTester, decoupledToDriver, parallel, testableClock}
import nl.tudelft.tydi_chisel._
import org.scalatest.flatspec.AnyFlatSpec

class PassthroughSpec extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "passthrough"

  class TydiPassthroughModule[T <: TydiEl](ioType: T, d: Int, width: Width) extends TydiModule {
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

  it should "pass through and reconstruct" in {
    test(new TydiPassthroughModule(new PostStreamGroup(), 1, 264.W)) { c =>
      c.in.initSource()
      c.out.initSink()

      c.out.expectInvalid()
      c.clock.step()
    }
  }
}
