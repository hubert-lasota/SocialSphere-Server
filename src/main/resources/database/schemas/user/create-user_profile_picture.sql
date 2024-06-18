CREATE TABLE user_profile_picture (
    id bigint primary key identity(1, 1),
    image_type varchar(50) not null,
    image varbinary(MAX) not null
);
