package nl.tudelft.post_processor

import scala.collection.immutable.SeqMap

trait InputStreamsAsFields[T] {
  val posts: T
  val post_titles: T
  val post_contents: T
  val post_author_username: T
  val post_tags: T
  val post_comments: T
  val post_comment_author_username: T
  val post_comment_content: T

  // Todo would be nice to be able to automatically get a map from the stream names to the field values.
  def toMap: SeqMap[String, T] = SeqMap(
    "posts" -> posts,
    "post_titles" -> post_titles,
    "post_contents" -> post_contents,
    "post_author_username" -> post_author_username,
    "post_tags" -> post_tags,
    "post_comments" -> post_comments,
    "post_comment_author_username" -> post_comment_author_username,
    "post_comment_content" -> post_comment_content
  )
}
