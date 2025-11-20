# Tydi Blog Post Processor Demo

This repo contains a demo hardware design written in Chisel that processes a list of blog posts using Tydi for stream communication.
Makes use of [Tydi-Chisel](https://github.com/abs-tudelft/Tydi-Chisel) for writing the hardware and [TydiPayloadKit](https://github.com/abs-tudelft/ScalaTydiPayloadKit) for testing.

The blog-post data that is being processed can be found in [posts.json](posts.json). The data structure of a post is as follows

- Post ID
- Title
- Content
- Author
  - User ID
  - Username
- Created timestamp
- Updated timestamp
- Tags
- Likes
- Shares
- Comments
  - Comment ID
  - Author
    - User ID
    - Username
  - Content
  - Created timestamp
  - Likes
  - \[optional\] Response to (ID)
