package org.techtown.myproject.comment

class CommentModel (
    val uid : String = "", // 댓글 작성자 uid
    val communityId : String = "", // 댓글이 작성된 게시글의 id
    val commentId : String = "", // 댓글의 id
    val commentContent : String = "",
    val commentTime : String = "",
    val count : String = "" // 대댓글 수
)