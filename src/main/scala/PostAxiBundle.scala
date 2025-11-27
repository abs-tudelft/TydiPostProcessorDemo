package nl.tudelft.post_processor

import chisel3._
import chisel3.util.DecoupledIO

class PostAxiBundle(laneCounts: PostStreamsAsFields[Int]) extends Bundle with PostStreamsAsFields[DecoupledIO[UInt]] {
  private val charSize = 16 // Rounding up charSize+1+d

  val posts: DecoupledIO[UInt] = DecoupledIO(UInt((264 * laneCounts.posts).W))
  val post_titles: DecoupledIO[UInt] = DecoupledIO(UInt((charSize * laneCounts.post_titles).W))
  val post_contents: DecoupledIO[UInt] = DecoupledIO(UInt((charSize * laneCounts.post_contents).W))
  val post_author_username: DecoupledIO[UInt] = DecoupledIO(UInt((charSize * laneCounts.post_author_username).W))
  val post_tags: DecoupledIO[UInt] = DecoupledIO(UInt((charSize * laneCounts.post_tags).W))
  val post_comments: DecoupledIO[UInt] = DecoupledIO(UInt((200 * laneCounts.post_comments).W))
  val post_comment_author_username: DecoupledIO[UInt] = DecoupledIO(UInt((charSize * laneCounts.post_comment_author_username).W))
  val post_comment_content: DecoupledIO[UInt] = DecoupledIO(UInt((charSize * laneCounts.post_comment_content).W))

  //  val asList: Seq[DecoupledIO[UInt]] = Seq(posts, post_titles, post_contents, post_author_username, post_tags, post_comments, post_comment_author_username, post_comment_content)
  val asList: Seq[DecoupledIO[UInt]] = elements.values.asInstanceOf[Iterable[DecoupledIO[UInt]]].toSeq.reverse
}
