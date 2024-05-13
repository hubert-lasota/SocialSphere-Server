CREATE TABLE post (
    id bigint primary key identity(1, 1),
    user_id bigint not null foreign key references users(id),
    content varchar(500),
    image VARBINARY
);