package nl.tudelft.post_processor

import PostTestUtils._
import general.TydiPassthroughSingleLane

import TydiPayloadKit.FromTydiBinary._
import TydiPayloadKit.CustomBinaryConversions._
import TydiPayloadKit.{TydiBinary, TydiBinaryStream, TydiStream}
import chisel3._
import chiseltest._
import nl.tudelft.tydi_chisel.BitsEl
import org.scalatest.flatspec.AnyFlatSpec

class PassthroughSpec extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "passthrough"

  it should "pass through and reconstruct" in {
    val globalWidth = 264

    test(new TydiPassthroughSingleLane(new PostStreamGroup(), 1, globalWidth.W)) { c =>
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

      postStream.posts.packets.zip(passedData).foreach { case (p, o) =>
        assert(p.data == o.litValue)
      }

      val passedLiterals = TydiBinaryStream(passedData.map(v => TydiBinary(v.litValue, globalWidth)))
      // val postDebinarizer = FromTydiBinary.gen[Post]
      val reconstructed = TydiStream.fromBinaryBlobs[Post](passedLiterals, 1)
      printPosts(reconstructed.toSeq.toList)
    }
  }

  it should "pass through higher dimensions" in {
    val globalWidth = 16

    test(new TydiPassthroughSingleLane(new BitsEl(8.W), 3, globalWidth.W)) { c =>
      c.in.initSource()
      c.out.initSink()

      val postStream = PostTestUtils.getPhysicalStreamsBinary
      c.out.ready.poke(true.B)

      val passedData = postStream.post_tags.map( p => {
        c.in.enqueueNow(p.data.U)
        val output = c.out.bits.peek()
        c.clock.step()
        output
      })

      postStream.post_tags.packets.zip(passedData).foreach { case (p, o) =>
        assert(p.data == o.litValue)
      }

      val passedLiterals = TydiBinaryStream(passedData.map(v => TydiBinary(v.litValue, globalWidth)))
      val reconstructed = TydiStream.fromBinaryBlobs[Char](passedLiterals, 3).unpackToStrings().unpackDim()
      println(reconstructed.toSeq)
    }
  }

  it should "pass through all streams" in {
    test(new PostPassthroughSingleLane) { c =>
      // Initialize input and output streams
      c.in.asList.foreach(_.initSource())
      c.out.asList.foreach(s => {
        s.initSink()
        s.ready.poke(true.B)
      })

      val postStream: InputPhysicalStreamsBinary = PostTestUtils.getPhysicalStreamsBinary
      val dataStreams = postStream.asList

      val passedData = c.in.asList.lazyZip(c.out.asList).lazyZip(dataStreams).lazyZip(postStream.names).map {
        case (in, out, stream, streamName) => TydiBinaryStream(
          stream.map(bin => {
            in.enqueueNow(bin.data.U)
            val output = out.bits.peek()
            val isComment = streamName == "post_comments"
            c.clock.step()
            TydiBinary(output.litValue, out.bits.getWidth)
          })
        )
      }.toList

      // Verify that the output data is the same as the input data
      postStream.asList.lazyZip(passedData).lazyZip(postStream.names).foreach { case (inStream, outStream, name) =>
        inStream.zip(outStream).zipWithIndex.foreach { case ((p, o), i) =>
          assert(p.data == o.data, s"Unequal data at index $i in stream $name: ${p.data} != ${o.data}")
        }
      }

      val outStream = InputPhysicalStreamsBinary(passedData(0), passedData(1), passedData(2), passedData(3), passedData(4), passedData(5), passedData(6), passedData(7))
      val reconstructed1 = outStream.reverse()
      val reconstructed2 = reconstructed1.reverse()
      printPosts(reconstructed2)
    }
  }
}
