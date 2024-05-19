CREATE TABLE post_image (
    id bigint primary key identity (1, 1),
    post_id bigint not null foreign key references post(id),
    image varbinary not null
)