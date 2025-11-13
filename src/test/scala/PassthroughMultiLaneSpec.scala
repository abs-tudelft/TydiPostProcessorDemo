package nl.tudelft.post_processor

import PostTestUtils._

import TydiPackaging.FromTydiBinary._
import TydiPackaging.{TydiBinary, TydiStream}
import chisel3._
import chisel3.experimental.VecLiterals._
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

      val postStream = PostTestUtils.getPhysicalStreamsBinary
      c.out.ready.poke(true.B)

      val inputData = postStream.post_tags.grouped(4).map(packets => {
        val mapped = packets.zipWithIndex.map(p => (p._2 -> p._1.data.U))
        val vecLit = Vec[UInt](n, UInt(globalWidth.W)).Lit(mapped: _*)
        vecLit.asUInt
      }).toSeq

      val passedData = inputData.map(packets => {
        c.in.enqueueNow(packets)
        val output = c.out.bits.peek()
        c.clock.step()
        output
      })

      postStream.post_tags.zip(passedData).foreach { case (p, o) =>
        assert(p.data == o.litValue)
      }

      val passedLiterals = passedData.map(v => TydiBinary(v.litValue, globalWidth))
      val reconstructed = TydiStream.fromBinaryBlobs[Char](passedLiterals, 3).unpackToStrings().unpackDim()
      println(reconstructed.toSeq)
    }
  }
}
