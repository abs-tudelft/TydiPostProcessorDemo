package nl.tudelft.post_processor
package active

import general._

import chisel3._
import nl.tudelft.tydi_chisel._

class PostProcessorMultiLane(laneCounts: InputStreamsAsFields[Int]) extends TydiModule with SimpleTypedIO[InputAxiBundle, OutputAxiBundle] {
  val in: InputAxiBundle = IO(Flipped(new InputAxiBundle(laneCounts)))
  private val inTydi: InputTydiPhysicalBundle = Wire(new InputTydiPhysicalBundle(laneCounts))
  val out: OutputAxiBundle = IO(new OutputAxiBundle(laneCounts))
  private val outTydi: OutputTydiPhysicalBundle = Wire(new OutputTydiPhysicalBundle(laneCounts))

  // Create converters from AXI to Tydi and connect signals.
  private val inConverters: Seq[Axi2TydiMultiLane] = in.asList.zip(inTydi.asList).map { case (axi, tydi) =>
    val converter = Module(new Axi2TydiMultiLane(axi.bits.getWidth / tydi.n, tydi))
    converter.input <> axi
    tydi := converter.output
    converter
  }

  // Create converters from Tydi to Axi and connect signals.
  private val outConverters: Seq[Tydi2AxiMultiLane] = out.asList.zip(outTydi.asList).map { case (axi, tydi) =>
    val converter = Module(new Tydi2AxiMultiLane(axi.bits.getWidth / tydi.n, tydi))
    converter.input := tydi
    axi <> converter.output
    converter
  }

  inTydi.asList.tail.zip(outTydi.asList.tail).foreach { case (inTydiLane, outTydiLane) =>
    outTydiLane := inTydiLane
  }

  private val metadataAdder = Module(new PostMetadataAddition)
  metadataAdder.in := inTydi.posts
  outTydi.posts := metadataAdder.out
}
