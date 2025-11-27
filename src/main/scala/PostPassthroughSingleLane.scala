package nl.tudelft.post_processor

import general.TydiPassthroughSingleLane

import chisel3._
import nl.tudelft.tydi_chisel.{TydiEl, TydiModule}

class PostPassthroughSingleLane extends TydiModule {
  private val laneCounts = PostStreamsSpecify(
    posts = 1,
    post_titles = 1,
    post_contents = 1,
    post_author_username = 4,
    post_tags = 1,
    post_comments = 1,
    post_comment_author_username = 1,
    post_comment_content = 1,
  )

  val in: PostAxiBundle = IO(Flipped(new PostAxiBundle(laneCounts)))
  val out: PostAxiBundle = IO(new PostAxiBundle(laneCounts))

  private val physicalStreams = new PostTydiPhysicalBundle

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
