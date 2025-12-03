package nl.tudelft.post_processor

import general.{SimpleTypedIO, TydiPassthroughMultiLane}

import chisel3._
import nl.tudelft.tydi_chisel.{TydiEl, TydiModule}

class PostPassthroughMultiLane(laneCounts: InputStreamsAsFields[Int]) extends TydiModule with SimpleTypedIO[InputAxiBundle, InputAxiBundle] {
  val in: InputAxiBundle = IO(Flipped(new InputAxiBundle(laneCounts)))
  val out: InputAxiBundle = IO(new InputAxiBundle(laneCounts))

  private val physicalStreams = new InputTydiPhysicalBundle

  private val laneCountValues = laneCounts.toMap.values.toSeq

  private val passthroughs: Seq[TydiPassthroughMultiLane[TydiEl]] = in.asList.lazyZip(physicalStreams.asList).lazyZip(laneCountValues).map { case (axi, tydi, n) =>
    Module(new TydiPassthroughMultiLane(tydi.elementType, tydi.d, n, axi.bits.getWidth/n))
  }

  in.asList.zip(passthroughs).foreach { case (io, module) =>
    module.in <> io
  }

  out.asList.zip(passthroughs).foreach { case (io, module) =>
    io <> module.out
  }
}
