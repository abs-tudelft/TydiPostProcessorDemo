package nl.tudelft.post_processor
package active

import chisel3._
import nl.tudelft.tydi_chisel._

class PostMetadataAddition extends SubProcessorBase(new PostStreamGroup, new PostWithMetadata, dIn = 1, dOut = 1) {
  outStream :@= inStream
  for ((outLane, inLane) <- outStream.data.zip(inStream.data)) {
    outLane.postId := inLane.postId
    outLane.createdAt := inLane.createdAt
    outLane.updatedAt := inLane.updatedAt
    outLane.likes := inLane.likes
    outLane.shares := inLane.shares
    outLane.metadata.interpunctions := 1.U
    outLane.metadata.lowercase := 2.U
    outLane.metadata.uppercase := 3.U
    outLane.metadata.spaces := 4.U
  }
}
