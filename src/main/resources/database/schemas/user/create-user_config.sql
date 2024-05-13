CREATE TABLE user_config (
    user_id bigint primary key foreign key references users(id),
    privacy_level varchar(15) check (privacy_level in ('PRIVATE', 'PUBLIC', 'FRIENDS'))
);
