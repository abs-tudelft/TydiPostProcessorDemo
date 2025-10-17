package nl.tudelft.post_processor

import chisel3._
import chisel3.util.DecoupledIO
import nl.tudelft.tydi_chisel.{BitsEl, PhysicalStream, TydiEl, TydiModule}

class PostPassthroughModule extends TydiModule {
  val in: PostAxiBundle = IO(Flipped(new PostAxiBundle))
  val out: PostAxiBundle = IO(new PostAxiBundle)

  private val physicalStreams = new PostTydiPhysicalBundle

  private val passthroughs: Seq[TydiPassthroughModule[TydiEl]] = in.asList.zip(physicalStreams.asList).map { case (axi, tydi) =>
    Module(new TydiPassthroughModule(tydi.elementType, tydi.d, axi.bits.getWidth.W))
  }.toSeq

  in.asList.zip(passthroughs).foreach { case (io, module) =>
    module.in <> io
  }

  out.asList.zip(passthroughs).foreach { case (io, module) =>
    io <> module.out
  }
}
