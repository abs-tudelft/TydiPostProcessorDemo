package nl.tudelft.post_processor

import PostTestUtils._

import TydiPackaging.FromTydiBinary._
import TydiPackaging.{TydiBinary, TydiBinaryStream, TydiPacket, TydiStream}
import chisel3._
import chiseltest._
import nl.tudelft.tydi_chisel.BitsEl
import org.scalatest.flatspec.AnyFlatSpec

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

    test(new TydiPassthroughMultiLane(new BitsEl(8.W), d, n, globalWidth)) { c =>
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
}
