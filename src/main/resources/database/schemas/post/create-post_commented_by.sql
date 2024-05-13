CREATE TABLE post_commented_by (
    post_id bigint foreign key references post(id),
    user_id bigint foreign key references users(id),
    primary key (post_id, user_id)
);