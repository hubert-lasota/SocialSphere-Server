CREATE TABLE post_comment (
    id bigint primary key identity(1, 1),
    post_id bigint not null foreign key references post(id),
    user_id bigint not null foreign key references users(id),
    content nvarchar(250) not null,
    created_at datetime2 not null,
    updated_at datetime2 not null
);