CREATE TABLE users (
    id bigint primary key identity(1, 1),
    username varchar(25) not null unique,
    password varchar(250) not null,
);
