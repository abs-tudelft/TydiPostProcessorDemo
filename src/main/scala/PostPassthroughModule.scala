package nl.tudelft.post_processor

import chisel3._
import chisel3.util.DecoupledIO
import nl.tudelft.tydi_chisel.{BitsEl, PhysicalStream, TydiEl, TydiModule}

class PostPassthroughModule extends TydiModule {
  val in: PostAxiBundle = IO(Flipped(new PostAxiBundle))
  val out: PostAxiBundle = IO(new PostAxiBundle)

  private val physicalStreams = new PostTydiPhysicalBundle

  private val passthroughs: Seq[TydiPassthroughModule[TydiEl]] = in.elements.zip(physicalStreams.elements).map { case ((_, axi: DecoupledIO[UInt]), (_, tydi: PhysicalStream)) =>
    new TydiPassthroughModule(tydi.elementType, tydi.d, axi.bits.getWidth.W)
  }.toSeq

  in.elements.values.asInstanceOf[Iterable[DecoupledIO[UInt]]].zip(passthroughs).foreach { case (io, module) =>
    module.in <> io
  }

  out.elements.map{case (_, d: DecoupledIO[UInt]) => d}.zip(passthroughs).foreach { case (io, module) =>
    io <> module.out
  }
}
