CREATE TABLE users (
    id bigint primary key identity(1, 1),
    username nvarchar(50) not null unique,
    password nvarchar(250) not null,
    online bit not null,
    created_at datetime2 not null,
    updated_at datetime2 not null,
    last_online_at datetime2
);
