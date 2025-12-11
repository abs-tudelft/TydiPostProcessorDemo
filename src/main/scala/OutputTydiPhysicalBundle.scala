package nl.tudelft.post_processor

import chisel3._
import nl.tudelft.tydi_chisel._

class OutputTydiPhysicalBundle(laneCounts: InputStreamsAsFields[Int]) extends Bundle with InputStreamsAsFields[PhysicalStream] {
  private val char = BitsEl(8.W)
  val posts: PhysicalStream = PhysicalStream(new PostWithMetadata, laneCounts.posts, 1, 1)
  val post_titles: PhysicalStream = PhysicalStream(char, laneCounts.post_titles, 2, 1)
  val post_contents: PhysicalStream = PhysicalStream(char, laneCounts.post_contents, 2, 1)
  val post_author_username: PhysicalStream = PhysicalStream(char, laneCounts.post_author_username, 2, 1)
  val post_tags: PhysicalStream = PhysicalStream(char, laneCounts.post_tags, 3, 1)
  val post_comments: PhysicalStream = PhysicalStream(new CommentsStreamGroup, laneCounts.post_comments, 2, 1)
  val post_comment_author_username: PhysicalStream = PhysicalStream(char, laneCounts.post_comment_author_username, 3, 1)
  val post_comment_content: PhysicalStream = PhysicalStream(char, laneCounts.post_comment_content, 3, 1)

  val asList: Seq[PhysicalStream] = elements.values.asInstanceOf[Iterable[PhysicalStream]].toSeq.reverse
}
