package nl.tudelft.post_processor

import chisel3._
import nl.tudelft.tydi_chisel._

class PostTydiPhysicalBundle extends Bundle{
  private val char = BitsEl(8.W)
  val posts: PhysicalStream = PhysicalStream(new PostStreamGroup, 1, 1, 1)
  val post_titles: PhysicalStream = PhysicalStream(char, 1, 2, 1)
  val post_contents: PhysicalStream = PhysicalStream(char, 1, 2, 1)
  val post_author_username: PhysicalStream = PhysicalStream(char, 1, 2, 1)
  val post_tags: PhysicalStream = PhysicalStream(char, 1, 3, 1)
  val post_comments: PhysicalStream = PhysicalStream(new CommentsStreamGroup, 1, 2, 1)
  val post_comment_author_username: PhysicalStream = PhysicalStream(char, 1, 3, 1)
  val post_comment_content: PhysicalStream = PhysicalStream(char, 1, 3, 1)

  val asList: Seq[PhysicalStream] = elements.values.asInstanceOf[Iterable[PhysicalStream]].toSeq.reverse
}
