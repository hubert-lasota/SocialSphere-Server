CREATE TABLE user_friend_list (
    friend_id bigint foreign key references users(id),
    inverse_friend_id bigint foreign key references users(id),
    primary key (friend_id, inverse_friend_id)
);