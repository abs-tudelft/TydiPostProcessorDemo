package nl.tudelft.post_processor

import PostTestUtils._

import TydiPackaging.FromTydiBinary._
import TydiPackaging.{TydiBinary, TydiStream}
import chisel3._
import chiseltest._
import nl.tudelft.tydi_chisel.BitsEl
import org.scalatest.flatspec.AnyFlatSpec

class PassthroughMultiLaneSpec extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "passthrough"

  it should "pass through higher dimensions" in {
    val globalWidth = 16
    val n = 4
    val d = 3

    test(new TydiPassthroughMultiLane(new BitsEl(8.W), d, n, globalWidth)) { c =>
      c.in.initSource()
      c.out.initSink()

      val tagsStream = PostTestUtils.getPhysicalStreamsBinary.post_tags
      c.out.ready.poke(true.B)

      val inputData = tagsStream.grouped(4).map(packets => {
        // Align packet width to AXI width and concatenate packets into a single literal
        val lit = packets.map(p => TydiBinary(p.data, globalWidth)).reduce(_.concat(_))
        lit.data.U
      }).toSeq

      val passedData = inputData.map(packets => {
        c.in.enqueueNow(packets)
        val output = c.out.bits.peek()
        c.clock.step()
        output
      })

      /*inputData.zip(passedData).foreach { case (p, o) =>
        assert(p.litValue == o.litValue)
      }*/

      val passedLiterals = passedData.map(v => TydiBinary(v.litValue, n*globalWidth))
      val passedPackets = passedLiterals.flatMap(lit => {
        // Split literal into n packets, each with width globalWidth
        val (packets, _) = (0 until n).foldLeft(List.empty[TydiBinary], lit) { case ((s, binary), i) =>
          val (el, remainder) = binary.splitLow(globalWidth)
          (el :: s, remainder)
        }
        packets.reverse
      })

      tagsStream.zip(passedPackets).foreach { case (p, o) =>
        assert(p.data == o.data)
      }

      val reconstructed = TydiStream.fromBinaryBlobs[Char](passedPackets, 3).unpackToStrings().unpackDim()
      println(reconstructed.toSeq)
    }
  }
}
