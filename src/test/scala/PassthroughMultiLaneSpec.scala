package nl.tudelft.post_processor

import PostTestUtils._

import TydiPayloadKit.FromTydiBinary._
import TydiPayloadKit.{TydiBinary, TydiBinaryStream, TydiPacket, TydiStream}
import chisel3._
import chiseltest._
import nl.tudelft.tydi_chisel.BitsEl
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.immutable.SeqMap

class PassthroughMultiLaneSpec extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "passthrough"

  it should "pass through tags for n=1" in {
    val globalWidth = 16

    test(new TydiPassthroughMultiLane(new BitsEl(8.W), 3, 1, globalWidth)) { c =>
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

  it should "pass through tags for n>1" in {
    val globalWidth = 16
    val n = 4
    val d = 3

    test(new TydiPassthroughMultiLane(new BitsEl(8.W), d, n, globalWidth)) { c =>
      c.in.initSource()
      c.out.initSink()

      val tagsStream: TydiBinaryStream = PostTestUtils.getPhysicalStreamsBinary.post_tags
      c.out.ready.poke(true.B)

      val inputData = tagsStream.group(n, globalWidth)

      val passedData = inputData.map(packets => {
        c.in.enqueueNow(packets.data.U)
        val output = c.out.bits.peek()
        c.clock.step()
        output
      })

      val passedLiterals = passedData.map(v => TydiBinary(v.litValue, n*globalWidth))
      val passedPackets = TydiBinaryStream.fromGrouped(passedLiterals, n)

      tagsStream.packets.zip(passedPackets.packets).foreach { case (p, o) =>
        assert(p.data == o.data)
      }

      val reconstructed = TydiStream.fromBinaryBlobs[Char](passedPackets, 3).unpackToStrings().unpackDim()
      // Filter out None packets and print reconstructed strings
      println(reconstructed.packets.filter(_.data.isDefined).map(_.data.get))
    }
  }



  it should "pass through test data" in {
    val globalWidth = 24
    val n = 3
    val d = 3

    test(new TydiPassthroughMultiLane(new BitsEl(16.W), d, n, globalWidth)) { c =>
      c.in.initSource()
      c.out.initSink()

      c.out.ready.poke(true.B)

      val inputPackets = Seq(
        TydiPacket(Some(1.toShort), Seq(true, false, true)),
        TydiPacket(Some(2.toShort), Seq(false, true, false)),
        TydiPacket(Some(3.toShort), Seq(true, true, true)),
        TydiPacket(Some(4.toShort), Seq(false, false, true)),
        TydiPacket(Some(5.toShort), Seq(false, true, false)),
        TydiPacket(Some(6.toShort), Seq(true, false, false)),
      )
      val inputBinaryPackets: TydiBinaryStream = TydiStream(inputPackets).toBinaryBlobs
      val inputGroupedPackets: TydiBinaryStream = inputBinaryPackets.group(n, globalWidth)

      val passedData = inputGroupedPackets.map(packets => {
        c.in.enqueueNow(packets.data.U)
        val output = c.out.bits.peek()
        c.clock.step()
        output
      })

      val passedLiterals = passedData.map(v => TydiBinary(v.litValue, n*globalWidth))
      val passedPackets = TydiBinaryStream.fromGrouped(passedLiterals, n)

      inputBinaryPackets.zip(passedPackets).foreach { case (p, o) =>
        assert(p.data == o.data)
      }
    }
  }

  it should "pass through all streams" in {
    val laneCounts = SeqMap(
      "posts" -> 1,
      "post_titles" -> 4,
      "post_contents" -> 4,
      "post_author_username" -> 4,
      "post_tags" -> 4,
      "post_comments" -> 1,
      "post_comment_author_username" -> 4,
      "post_comment_content" -> 4,
    )

    test(new PostPassthroughMultiLane(laneCounts)) { c =>
      // Initialize input and output streams
      c.in.asList.foreach(_.initSource())
      c.out.asList.foreach(s => {
        s.initSink()
        s.ready.poke(true.B)
      })

      val postStream: PhysicalStreamsBinary = PostTestUtils.getPhysicalStreamsBinary
      val dataStreams = postStream.asList

      val dataOutRaw = c.in.asList.lazyZip(c.out.asList).lazyZip(dataStreams).lazyZip(postStream.names).map {
        case (in, out, stream, streamName) => TydiBinaryStream({
          val n = laneCounts(streamName)
          val blobWidth = in.bits.getWidth/n
          stream.group(n, blobWidth).map(bin => {
            in.enqueueNow(bin.data.U)
            val output = out.bits.peek()
            c.clock.step()
            TydiBinary(output.litValue, out.bits.getWidth)
          })
        })
      }.toList

      val dataOut = dataOutRaw.zip(postStream.names).map { case (stream, name) => stream.ungroup(laneCounts(name)) }

      // Verify that the output data is the same as the input data
      postStream.asList.lazyZip(dataOut).lazyZip(postStream.names).foreach { case (inStream, outStream, name) =>
        inStream.zip(outStream).zipWithIndex.foreach { case ((p, o), i) =>
          assert(p.data == o.data, s"Unequal data at index $i in stream $name: ${p.data} != ${o.data}")
        }
      }

      val outStream = PhysicalStreamsBinary(dataOut(0), dataOut(1), dataOut(2), dataOut(3), dataOut(4), dataOut(5), dataOut(6), dataOut(7))
      val reconstructed1 = outStream.reverse()
      val reconstructed2 = reconstructed1.reverse()
      printPosts(reconstructed2)
    }
  }

  it should "use the test hardness" in {
    val laneCounts = SeqMap(
      "posts" -> 1,
      "post_titles" -> 4,
      "post_contents" -> 4,
      "post_author_username" -> 4,
      "post_tags" -> 4,
      "post_comments" -> 1,
      "post_comment_author_username" -> 4,
      "post_comment_content" -> 4,
    )

    test(new TestHarness(laneCounts, new PostPassthroughMultiLane(laneCounts))) { c =>
      val postStream: PhysicalStreamsBinary = PostTestUtils.getPhysicalStreamsBinary
      val dataStreams = postStream.asList
      val streamLengths = postStream.asTuplesWithNames.map { case (name, stream) =>
        stream.packets.length / laneCounts(name)
      }
      val maxLength = streamLengths.max

      // All data will flow from the ROM automatically, we just need to wait until the last packet is sent.
      step(maxLength)

      /*while (c.lengths(0).peek().litValue < postStream.posts.packets.length) {
        step(1)
      }*/

      val dataOutRaw: List[TydiBinaryStream] = c.output.map(out => {
        TydiBinaryStream({
          val output: Vec[UInt] = out.peek
          output.map(v =>
            TydiBinary(v.litValue, v.getWidth)
          )
        })
      }).toList

      val dataOut = dataOutRaw.zip(postStream.names).map { case (stream, name) => stream.ungroup(laneCounts(name)) }

      // Verify that the output data is the same as the input data
      postStream.asList.lazyZip(dataOut).lazyZip(postStream.names).foreach { case (inStream, outStream, name) =>
        inStream.zip(outStream).zipWithIndex.foreach { case ((p, o), i) =>
          assert(p.data == o.data, s"Unequal data at index $i in stream $name: ${p.data} != ${o.data}")
        }
      }

      val outStream = PhysicalStreamsBinary(dataOut(0), dataOut(1), dataOut(2), dataOut(3), dataOut(4), dataOut(5), dataOut(6), dataOut(7))
      val reconstructed1 = outStream.reverse()
      val reconstructed2 = reconstructed1.reverse()
      printPosts(reconstructed2)
    }
  }
}
