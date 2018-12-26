drop table if exists user;

create table user
(
  id    BIGINT        not null,
  name  VARCHAR(200)  default NULL,
  primary key (id)
);