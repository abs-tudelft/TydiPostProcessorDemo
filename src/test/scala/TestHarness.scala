package nl.tudelft.post_processor

import TydiPayloadKit.TydiBinaryStream
import chisel3._
import chisel3.util._
import PostTestUtils._

import scala.collection.immutable.SeqMap

class TestHarness[A <: Bundle, B <: Bundle](module: => SimpleTypedIO[A, B], inStreams: SeqMap[String, TydiBinaryStream], outCapacities: SeqMap[String, Int]) extends Module {
  private val dut = Module(module)

  private val inList = dut.in.elements.values.asInstanceOf[Iterable[DecoupledIO[UInt]]].toSeq.reverse
  private val outList = dut.out.elements.values.asInstanceOf[Iterable[DecoupledIO[UInt]]].toSeq.reverse

  private val streams = inStreams.values.toSeq
  private val names = inStreams.keys.toSeq

  private val inputRoms = streams.lazyZip(names).lazyZip(inList).map { case (stream, name, axi) =>
    Module(new AxiSourceRom(stream, axi.bits.getWidth.W, name))
  }
  inputRoms.map(_.output).zip(inList).foreach{ case (rom, axi) => axi <> rom }

  private val outputMems = names.zip(outList).map { case (name, axi) =>
    Module(new AxiSinkMem(outCapacities(name), axi.bits.getWidth.W, name))
  }
  outputMems.map(_.input).zip(outList).foreach{ case (mem, axi) => mem <> axi }

  val outputData: MixedVec[Vec[UInt]] = MixedVecInit(outputMems.map(_.data))
  val output: MixedVec[Vec[UInt]] = IO(outputData.cloneType)
  output := outputData
  private val lengthsData: Vec[UInt] = VecInit(outputMems.map(_.length))
  val lengths: Vec[UInt] = IO(lengthsData.cloneType)
  lengths := lengthsData
}
