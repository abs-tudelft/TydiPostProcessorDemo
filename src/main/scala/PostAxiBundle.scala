package nl.tudelft.post_processor

import chisel3._
import chisel3.util.DecoupledIO

class PostAxiBundle extends Bundle {
  private val charData = UInt(16.W) // Rounding up 8+1+d
  val posts: DecoupledIO[UInt] = DecoupledIO(UInt(264.W))
  val post_titles: DecoupledIO[UInt] = DecoupledIO(charData)
  val post_contents: DecoupledIO[UInt] = DecoupledIO(charData)
  val post_author_username: DecoupledIO[UInt] = DecoupledIO(charData)
  val post_tags: DecoupledIO[UInt] = DecoupledIO(charData)
  val post_comments: DecoupledIO[UInt] = DecoupledIO(UInt(200.W))
  val post_comment_author_username: DecoupledIO[UInt] = DecoupledIO(charData)
  val post_comment_content: DecoupledIO[UInt] = DecoupledIO(charData)

//  val asList: Seq[DecoupledIO[UInt]] = Seq(posts, post_titles, post_contents, post_author_username, post_tags, post_comments, post_comment_author_username, post_comment_content)
  val asList: Seq[DecoupledIO[UInt]] = elements.values.asInstanceOf[Iterable[DecoupledIO[UInt]]].toSeq.reverse
}
