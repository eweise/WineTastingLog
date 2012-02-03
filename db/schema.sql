# --- First database schema

# --- !Ups

create sequence user_seq start with 1000;

create table users (id bigint not null, email varchar(255), username varchar(255), password varchar(255), createDate date, token varchar(255));

insert into users(id, email, username, password) values(1, 'eweise@gmail.com', 'eweise', 'password', '2011-01-01');

create sequence tasting_seq start with 1000;

create table tasting (id bigint not null, userId bigint, rating integer, notes varchar(1000), brand varchar(255), style varchar(255), region varchar(255), year integer, updateDate date, constraint pk_tasting primary key (id));

insert into tasting(id, userId, rating, notes, brand, style, region, year) values(1, 1, 3, 'nice nose', 'Gallo', 'Chardonney','Napa', 1997);

# --- !Downs
drop table if exists tasting;

drop sequence if exists tasting_seq;

