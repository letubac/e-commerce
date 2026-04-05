UPDATE reviews SET
    rating          = /*review.rating*/,
    title           = /*review.title*/,
    comment         = /*review.comment*/,
    is_anonymous    = /*review.isAnonymous*/,
    updated_at      = /*review.updatedAt*/
WHERE id = /*review.id*/