CREATE TABLE user_profile_config (
    user_id bigint primary key foreign key references users(id),
    profile_privacy_level varchar(15) check (profile_privacy_level in ('PRIVATE', 'PUBLIC', 'FRIENDS'))
);
