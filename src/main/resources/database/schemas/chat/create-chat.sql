CREATE TABLE chat(
    id bigint primary key identity (1, 1),
    created_at datetime2 not null,
    last_message datetime2
);