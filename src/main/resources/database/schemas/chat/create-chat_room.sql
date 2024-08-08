CREATE TABLE chat_room(
    chat_id bigint not null foreign key references chat(id),
    user_id bigint not null foreign key references users(id),
    primary key (chat_id, user_id)
);