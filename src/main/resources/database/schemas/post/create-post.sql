CREATE TABLE post (
    id bigint primary key identity(1, 1),
    user_id bigint not null foreign key references users(id),
    content nvarchar(500),
    like_count bigint not null,
    comment_count bigint not null,
    created_at datetime2 not null,
    updated_at datetime2 not null
);