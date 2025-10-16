package nl.tudelft.post_processor

import TydiPackaging.{FromTydiBinary, TydiBinary, TydiStream}
import TydiPackaging.FromTydiBinary._
import TydiPackaging.CustomBinaryConversions._
import chisel3._
import chisel3.util.DecoupledIO
import chiseltest._
import nl.tudelft.tydi_chisel._
import org.scalatest.flatspec.AnyFlatSpec
import PostTestUtils._

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
    val globalWidth = 264

    test(new TydiPassthroughModule(new PostStreamGroup(), 1, globalWidth.W)) { c =>
      c.in.initSource()
      c.out.initSink()

      val postStream = PostTestUtils.getPhysicalStreamsBinary
      c.out.ready.poke(true.B)

      val passedData = postStream.posts.map( p => {
        c.in.enqueueNow(p.data.U)
        val output = c.out.bits.peek()
        c.clock.step()
        output
      })

      val passedLiterals = passedData.map(v => TydiBinary(v.litValue, globalWidth))
      // val postDebinarizer = FromTydiBinary.gen[Post]
      val reconstructed = TydiStream.fromBinaryBlobs[Post](passedLiterals, 1)
      printPosts(reconstructed.toSeq.toList)
    }
  }
}
