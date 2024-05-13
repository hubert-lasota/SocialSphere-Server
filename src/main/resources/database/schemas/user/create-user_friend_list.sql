CREATE TABLE user_friend_list (
    user_id bigint foreign key references users(id),
    user_friend_id bigint foreign key references users(id),
    primary key (user_id, user_friend_id)
);