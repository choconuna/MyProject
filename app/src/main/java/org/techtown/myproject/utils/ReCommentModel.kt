package org.techtown.myproject.utils

class ReCommentModel (
    val uid : String = "", // 댓글 작성자 uid
    val communityId : String = "", // 댓글이 작성된 게시글의 id
    val commentId : String = "", // 댓글의 id
    val reCommentId : String = "", // 대댓글의 id
    val reCommentContent : String = "",
    val reCommentTime : String = ""
)