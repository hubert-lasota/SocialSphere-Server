CREATE TABLE user_profile (
    user_id bigint primary key foreign key references users(id),
    profile_picture_id bigint foreign key references user_profile_picture(upp_id),
    first_name nvarchar(25),
    last_name nvarchar(25),
    city nvarchar(75),
    country nvarchar(75)
);