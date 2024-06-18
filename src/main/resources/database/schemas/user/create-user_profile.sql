CREATE TABLE user_profile (
    user_id bigint primary key foreign key references users(id),
    profile_picture_id bigint foreign key references user_profile_picture(id),
    first_name varchar(25),
    last_name varchar(25),
    city varchar(75),
    country varchar(75)
);