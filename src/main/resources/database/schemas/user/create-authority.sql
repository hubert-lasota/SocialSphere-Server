CREATE TABLE authority (
    user_id bigint primary key foreign key references users(id),
    authority varchar(25) not null
);