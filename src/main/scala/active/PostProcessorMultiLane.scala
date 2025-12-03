package nl.tudelft.post_processor
package active

import general._

import chisel3._
import nl.tudelft.tydi_chisel._

class PostProcessorMultiLane(laneCounts: InputStreamsAsFields[Int]) extends TydiModule with SimpleTypedIO[InputAxiBundle, OutputAxiBundle] {
  val in: InputAxiBundle = IO(Flipped(new InputAxiBundle(laneCounts)))
  val out: OutputAxiBundle = IO(new OutputAxiBundle(laneCounts))

  private val physicalStreams = new InputTydiPhysicalBundle

  private val laneCountValues = laneCounts.toMap.values.toSeq

  private val passthroughs: Seq[TydiPassthroughMultiLane[TydiEl]] = in.asList.lazyZip(physicalStreams.asList).lazyZip(laneCountValues).tail.map { case (axi, tydi, n) =>
    Module(new TydiPassthroughMultiLane(tydi.elementType, tydi.d, n, axi.bits.getWidth/n))
  }.toSeq

  private val metadataAdder = Module(new PostMetadataAddition)
  // Fixme: This process is really inconvenient, must find a way to make all streams available automatically.
  private val postStream = Wire(PhysicalStream(new PostStreamGroup, 1, 1, 1))
  private val postWithMetadataStream = Wire(PhysicalStream(new PostWithMetadata, 1, 1, 1))
  private val inConverter = Module(new Axi2TydiMultiLane(in.posts.getWidth, postStream))
  private val outConverter = Module(new Tydi2AxiMultiLane(in.posts.getWidth, postWithMetadataStream))
  inConverter.input <> in.posts
  postStream := inConverter.output
  metadataAdder.in := postStream
  outConverter.input := metadataAdder.out
  out.posts <> outConverter.output

  in.asList.zip(passthroughs).foreach { case (io, module) =>
    module.in <> io
  }

  out.asList.zip(passthroughs).foreach { case (io, module) =>
    io <> module.out
  }
}
