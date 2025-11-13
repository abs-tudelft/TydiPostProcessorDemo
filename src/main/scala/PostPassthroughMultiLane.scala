package nl.tudelft.post_processor

import chisel3._
import nl.tudelft.tydi_chisel.{TydiEl, TydiModule}

class PostPassthroughMultiLane extends TydiModule {
  val in: PostAxiBundle = IO(Flipped(new PostAxiBundle))
  val out: PostAxiBundle = IO(new PostAxiBundle)

  private val physicalStreams = new PostTydiPhysicalBundle

  private val passthroughs: Seq[TydiPassthroughMultiLane[TydiEl]] = in.asList.zip(physicalStreams.asList).map { case (axi, tydi) =>
    // Use multiple lanes for string streams, otherwise use a single lane
    val n = if (tydi.elWidth == 8) { 4 } else { 1 }
    Module(new TydiPassthroughMultiLane(tydi.elementType, tydi.d, n, axi.bits.getWidth))
  }.toSeq

  in.asList.zip(passthroughs).foreach { case (io, module) =>
    module.in <> io
  }

  out.asList.zip(passthroughs).foreach { case (io, module) =>
    io <> module.out
  }
}
