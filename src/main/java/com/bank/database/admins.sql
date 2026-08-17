DROP TABLE IF EXISTS admins;

CREATE TABLE admins(
  admin_id serial primary key,
  admin_first_name varchar(20) not null,
  admin_last_name varchar(20) not null,
  admin_mail varchar(60) unique not null,
  admin_password varchar(15) not null
);