CREATE TABLE user_profile (
    user_id bigint primary key foreign key references users(id),
    first_name varchar(25),
    last_name varchar(25),
    city varchar(25),
    country varchar(25)
);