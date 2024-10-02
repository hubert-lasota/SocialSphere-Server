CREATE TABLE user_friend_request(
    id bigint primary key identity (1, 1),
    sender_id bigint not null foreign key references users(id),
    receiver_id bigint not null foreign key references users(id),
    status varchar(25) not null check (status in ('ACCEPTED', 'REJECTED', 'WAITING_FOR_RESPONSE')),
    sent_at datetime2 not null,
    replied_at datetime2
);