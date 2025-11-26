package nl.tudelft.post_processor

import TydiPayloadKit.TydiBinaryStream
import chisel3._
import chisel3.util._
import PostTestUtils._

import scala.collection.immutable.SeqMap

class TestHarness(laneCounts: SeqMap[String, Int]) extends Module {
  private val passthroughModule = Module(new PostPassthroughMultiLane(laneCounts))

  private val postStream: PhysicalStreamsBinary = PostTestUtils.getPhysicalStreamsBinary

  private val inputRoms = postStream.asList.lazyZip(postStream.names).lazyZip(passthroughModule.in.asList).map { case (stream, name, axi) =>
    val laneCount = laneCounts(name)
    val blobWidth = axi.bits.getWidth
    val romStream = stream.group(laneCount, blobWidth/laneCount)
    Module(new AxiSourceRom(romStream, blobWidth.W))
  }
  inputRoms.map(_.output).zip(passthroughModule.in.asList).foreach{ case (rom, axi) => axi <> rom }

  private val outputMems = postStream.asList.lazyZip(postStream.names).lazyZip(passthroughModule.in.asList).map { case (stream, name, axi) =>
    val capacity = stream.packets.length
    val laneCount = laneCounts(name)
    val blobWidth = axi.bits.getWidth
    Module(new AxiSinkMem(capacity, blobWidth.W))
  }
  outputMems.map(_.input).zip(passthroughModule.out.asList).foreach{ case (mem, axi) => mem <> axi }

  val outputData: MixedVec[Vec[UInt]] = MixedVecInit(outputMems.map(_.data))
  val output: MixedVec[Vec[UInt]] = IO(outputData.cloneType)
  output := outputData
  private val lengthsData: Vec[UInt] = VecInit(outputMems.map(_.length))
  val lengths: Vec[UInt] = IO(lengthsData.cloneType)
  lengths := lengthsData
}
