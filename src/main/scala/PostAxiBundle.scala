package nl.tudelft.post_processor

import chisel3._
import chisel3.util.DecoupledIO

class PostAxiBundle extends Bundle {
  private val char = UInt(8.W)
  val posts: DecoupledIO[UInt] = DecoupledIO(UInt(264.W))
  val post_titles: DecoupledIO[UInt] = DecoupledIO(char)
  val post_contents: DecoupledIO[UInt] = DecoupledIO(char)
  val post_author_username: DecoupledIO[UInt] = DecoupledIO(char)
  val post_tags: DecoupledIO[UInt] = DecoupledIO(char)
  val post_comments: DecoupledIO[UInt] = DecoupledIO(UInt(200.W))
  val post_comment_author_username: DecoupledIO[UInt] = DecoupledIO(char)
  val post_comment_content: DecoupledIO[UInt] = DecoupledIO(char)
}
