package nl.tudelft.post_processor

import TydiPackaging.{FromTydiBinary, TydiBinary, TydiStream}
import TydiPackaging.FromTydiBinary._
import TydiPackaging.CustomBinaryConversions._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import PostTestUtils._

class PassthroughSpec extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "passthrough"

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

      postStream.posts.zip(passedData).foreach { case (p, o) =>
        assert(p.data == o.litValue)
      }

      val passedLiterals = passedData.map(v => TydiBinary(v.litValue, globalWidth))
      // val postDebinarizer = FromTydiBinary.gen[Post]
      val reconstructed = TydiStream.fromBinaryBlobs[Post](passedLiterals, 1)
      printPosts(reconstructed.toSeq.toList)
    }
  }
}
