CREATE TABLE chat(
    id bigint primary key identity (1, 1),
    created_by_id bigint not null foreign key references users(id),
    last_message_id bigint /* altered in create-chat_message.sql */,
    created_at datetime2 not null,
);