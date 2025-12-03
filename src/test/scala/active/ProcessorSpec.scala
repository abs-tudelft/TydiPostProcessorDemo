package nl.tudelft.post_processor
package active

import PostTestUtils._
import general.{TestHarness, TydiPassthroughMultiLane}

import TydiPayloadKit.FromTydiBinary._
import TydiPayloadKit.{TydiBinary, TydiBinaryStream, TydiPacket, TydiStream}
import chisel3._
import chiseltest._
import nl.tudelft.tydi_chisel.BitsEl
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.immutable.SeqMap

class ProcessorSpec extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "passthrough"

  it should "use the test hardness" in {
    val laneCounts = InputStreamsSpecify(
      posts = 1,
      post_titles = 4,
      post_contents = 4,
      post_author_username = 4,
      post_tags = 4,
      post_comments = 1,
      post_comment_author_username = 4,
      post_comment_content = 4,
    )

    val postStream: InputPhysicalStreamsBinary = PostTestUtils.getPhysicalStreamsBinary
    val bundle = new InputAxiBundle(laneCounts)

    // Create a map of input streams that have their packets grouped by lane count
    val inputStreams: SeqMap[String, TydiBinaryStream] = SeqMap.from(
      postStream.asList.lazyZip(postStream.names).lazyZip(bundle.asList).map { case (stream, name, axi) =>
        val laneCount = laneCounts.toMap(name)
        val blobWidth = axi.bits.getWidth
        (name -> stream.group(laneCount, blobWidth / laneCount))
      }
    )

    // Calculate the output capacity of each stream
    val outCapacity: SeqMap[String, Int] = inputStreams.map { case (name, stream) => (name -> stream.packets.length) }

    test(new TestHarness(new PostProcessorMultiLane(laneCounts), inputStreams, outCapacity)) { c =>
      val maxLength = outCapacity.values.max

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

      val dataOut = dataOutRaw.zip(postStream.names).map { case (stream, name) => stream.ungroup(laneCounts.toMap(name)) }

      // Verify that the output data is the same as the input data
      postStream.asList.lazyZip(dataOut).lazyZip(postStream.names).foreach { case (inStream, outStream, name) =>
        inStream.zip(outStream).zipWithIndex.foreach { case ((p, o), i) =>
          assert(p.data == o.data, s"Unequal data at index $i in stream $name: ${p.data} != ${o.data}")
        }
      }

      val outStream = OutputPhysicalStreamsBinary(dataOut(0), dataOut(1), dataOut(2), dataOut(3), dataOut(4), dataOut(5), dataOut(6), dataOut(7))
      val reconstructed1 = outStream.reverse()
      val reconstructed2 = reconstructed1.reverse()
      print(reconstructed2)
    }
  }
}
