CREATE TABLE user_friend_request(
    sender_id bigint not null foreign key references users(id),
    receiver_id bigint not null foreign key references users(id),
    status varchar(25) not null check (status in ('ACCEPTED', 'REJECTED', 'WAITING_FOR_RESPONSE')),
    primary key (sender_id, receiver_id)
);