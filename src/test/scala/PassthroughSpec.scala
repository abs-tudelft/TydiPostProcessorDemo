package nl.tudelft.post_processor

import TydiPackaging.{FromTydiBinary, TydiBinary, TydiStream}
import TydiPackaging.FromTydiBinary._
import TydiPackaging.CustomBinaryConversions._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import PostTestUtils._

import chisel3.util.DecoupledIO

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

  it should "pass through all streams" in {
    test(new PostPassthroughModule) { c =>
      // Initialize input and output streams
      c.in.asList.foreach(_.initSource())
      c.out.asList.foreach(s => {
        s.initSink()
        s.ready.poke(true.B)
      })

      val postStream: PhysicalStreamsBinary = PostTestUtils.getPhysicalStreamsBinary
      val dataStreams = postStream.asList

      val passedData = c.in.asList.lazyZip(c.out.asList).lazyZip(dataStreams).map {
        case (in, out, stream) => stream.map(bin => {
          in.enqueueNow(bin.data.U)
          val output = out.bits.peek()
          c.clock.step()
          TydiBinary(output.litValue, out.bits.getWidth)
        })
      }.toList

      postStream.posts.zip(passedData(0)).foreach { case (p, o) =>
        assert(p.data == o.data)
      }

      val outStream = PhysicalStreamsBinary(passedData(0), passedData(1), passedData(2), passedData(3), passedData(4), passedData(5), passedData(6), passedData(7))
      val reconstructed1 = outStream.reverse()
      val reconstructed2 = reconstructed1.reverse()
      printPosts(reconstructed2)
    }
  }
}
