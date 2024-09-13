CREATE TABLE chat_message(
    id bigint primary key identity (1, 1),
    chat_id bigint not null foreign key references chat(id),
    sender_id bigint not null foreign key references users(id),
    content varchar(250) not null,
    created_at datetime2 not null,
    seen bit not null
);