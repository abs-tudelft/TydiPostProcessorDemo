package nl.tudelft.post_processor

import chisel3._
import nl.tudelft.tydi_chisel.{TydiEl, TydiModule}

import scala.collection.immutable.SeqMap

class PostPassthroughMultiLane(laneCounts: SeqMap[String, Int]) extends TydiModule with SimpleTypedIO[PostAxiBundle, PostAxiBundle] {
  val in: PostAxiBundle = IO(Flipped(new PostAxiBundle(laneCounts)))
  val out: PostAxiBundle = IO(new PostAxiBundle(laneCounts))

  private val physicalStreams = new PostTydiPhysicalBundle

  private val laneCountValues = laneCounts.values.toSeq

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
