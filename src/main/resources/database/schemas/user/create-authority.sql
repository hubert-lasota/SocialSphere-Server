CREATE TABLE authority (
    id bigint primary key identity (1, 1),
    user_id bigint not null foreign key references users(id),
    authority varchar(25) not null
);