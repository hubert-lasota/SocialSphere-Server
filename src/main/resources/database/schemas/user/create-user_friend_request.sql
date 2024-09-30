CREATE TABLE user_friend_request(
    id bigint primary key identity (1, 1),
    sender_id bigint not null foreign key references users(id),
    receiver_id bigint not null foreign key references users(id),
    status varchar(25) not null check (status in ('ACCEPTED', 'REJECTED', 'WAITING_FOR_RESPONSE')),
    sent_at datetime2 not null,
    reply_at datetime2,
    CONSTRAINT UQ_sender_receiver UNIQUE (sender_id, receiver_id)
);