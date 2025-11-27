package nl.tudelft.post_processor

import TydiPayloadKit.TydiBinaryStream
import chisel3._
import chisel3.util._
import PostTestUtils._

import scala.collection.immutable.SeqMap

class TestHarness[A <: Bundle, B <: Bundle](laneCounts: SeqMap[String, Int], module: => SimpleTypedIO[A, B]) extends Module {
  private val dut = Module(module)

  private val postStream: PhysicalStreamsBinary = PostTestUtils.getPhysicalStreamsBinary

  private val inList = dut.in.elements.values.asInstanceOf[Iterable[DecoupledIO[UInt]]].toSeq.reverse
  private val outList = dut.out.elements.values.asInstanceOf[Iterable[DecoupledIO[UInt]]].toSeq.reverse

  private val inputRoms = postStream.asList.lazyZip(postStream.names).lazyZip(inList).map { case (stream, name, axi) =>
    val laneCount = laneCounts(name)
    val blobWidth = axi.bits.getWidth
    val romStream = stream.group(laneCount, blobWidth/laneCount)
    Module(new AxiSourceRom(romStream, blobWidth.W))
  }
  inputRoms.map(_.output).zip(inList).foreach{ case (rom, axi) => axi <> rom }

  private val outputMems = postStream.asList.lazyZip(postStream.names).lazyZip(outList).map { case (stream, name, axi) =>
    val laneCount = laneCounts(name)
    val capacity = math.ceil(stream.packets.length.toDouble/laneCount).toInt
    val blobWidth = axi.bits.getWidth
    Module(new AxiSinkMem(capacity, blobWidth.W))
  }
  outputMems.map(_.input).zip(outList).foreach{ case (mem, axi) => mem <> axi }

  val outputData: MixedVec[Vec[UInt]] = MixedVecInit(outputMems.map(_.data))
  val output: MixedVec[Vec[UInt]] = IO(outputData.cloneType)
  output := outputData
  private val lengthsData: Vec[UInt] = VecInit(outputMems.map(_.length))
  val lengths: Vec[UInt] = IO(lengthsData.cloneType)
  lengths := lengthsData
}
