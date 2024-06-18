CREATE TABLE post_image (
    id bigint primary key identity (1, 1),
    post_id bigint not null foreign key references post(id),
    type varchar(25) not null,
    image varbinary(MAX) not null
)