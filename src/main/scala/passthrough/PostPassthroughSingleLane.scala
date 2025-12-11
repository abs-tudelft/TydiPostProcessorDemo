package nl.tudelft.post_processor
package passthrough

import general.TydiPassthroughSingleLane

import chisel3._
import nl.tudelft.tydi_chisel.{TydiEl, TydiModule}

class PostPassthroughSingleLane extends TydiModule {
  private val laneCounts = InputStreamsSpecify(
    posts = 1,
    post_titles = 1,
    post_contents = 1,
    post_author_username = 4,
    post_tags = 1,
    post_comments = 1,
    post_comment_author_username = 1,
    post_comment_content = 1,
  )

  val in: InputAxiBundle = IO(Flipped(new InputAxiBundle(laneCounts)))
  val out: InputAxiBundle = IO(new InputAxiBundle(laneCounts))

  private val physicalStreams = new InputTydiPhysicalBundle(laneCounts)

  private val passthroughs: Seq[TydiPassthroughSingleLane[TydiEl]] = in.asList.zip(physicalStreams.asList).map { case (axi, tydi) =>
    Module(new TydiPassthroughSingleLane(tydi.elementType, tydi.d, axi.bits.getWidth.W))
  }.toSeq

  in.asList.zip(passthroughs).foreach { case (io, module) =>
    module.in <> io
  }

  out.asList.zip(passthroughs).foreach { case (io, module) =>
    io <> module.out
  }
}
