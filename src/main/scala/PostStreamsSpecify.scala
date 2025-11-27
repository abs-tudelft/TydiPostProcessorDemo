package nl.tudelft.post_processor

case class PostStreamsSpecify[T](
                                  posts: T,
                                  post_titles: T,
                                  post_contents: T,
                                  post_author_username: T,
                                  post_tags: T,
                                  post_comments: T,
                                  post_comment_author_username: T,
                                  post_comment_content: T,
                                ) extends PostStreamsAsFields[T]
