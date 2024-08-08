CREATE TABLE post_notification (
    id bigint primary key identity (1, 1),
    post_id bigint not null foreign key references post(id),
    updated_by bigint not null foreign key references users(id),
    update_type varchar(50) not null check (update_type in ('LIKE', 'COMMENT')),
    updated_at datetime2 not null
);