package nl.tudelft.post_processor

import chisel3._
import nl.tudelft.tydi_chisel._

class StringStream(c: Int, d: Int, n: Int, r: Boolean) extends PhysicalStreamDetailed(BitsEl(8.W), n, d, c, r)

class AuthorGroup extends Group {
  val userId = UInt(32.W)
  val username = new StringStream(c=8, d=1, n=4, r=false)
}

class InReplyToCommentIdUnion extends Union(2) {
  val null_value = Null()
  val value = UInt(32.W)
}

class CommentsStreamGroup extends Group {
  val commentId = UInt(32.W)
  val author = new AuthorGroup
  val content = new StringStream(c=8, d=1, n=4, r=false)
  val createdAt = UInt(64.W)
  val likes = UInt(32.W)
  val inReplyToCommentId = new InReplyToCommentIdUnion
}

class CommentsStream extends PhysicalStreamDetailed(e=new CommentsStreamGroup, c=8, d=1, n=1, r=false)

class PostStreamGroup extends Group {
  val postId = UInt(32.W)
  val title = new StringStream(c=8, d=1, n=4, r=false)
  val content = new StringStream(c=8, d=1, n=4, r=false)
  val author = new AuthorGroup
  val createdAt = UInt(64.W)
  val updatedAt = UInt(64.W)
  val tags = new StringStream(c=8, d=2, n=4, r=false)
  val likes = UInt(32.W)
  val shares = UInt(32.W)
  val comments = new CommentsStream
}

class PostStream extends PhysicalStreamDetailed(e=new PostStreamGroup, c=8, d=1, n=1, r=false)

class RootStreamlet extends TydiModule {
  private val outputStream = new PostStream
  val output: PhysicalStream = outputStream.toPhysical
}
