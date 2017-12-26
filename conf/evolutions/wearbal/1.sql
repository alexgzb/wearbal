# --- First database schema

# --- !Ups

set ignorecase true;

create table company (
  id bigint not null,
  name varchar(255) not null,
  constraint pk_company primary key (id))
;

CREATE TABLE product (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  description varchar(255) NOT NULL,
  sku varchar(255) NOT NULL,
  ean varchar(255),
  num_in_stock INTEGER NOT NULL,
  purchase_price DOUBLE NOT NULL,
  selling_price DOUBLE,
  rrp DOUBLE,
  brand VARCHAR(255),
  company_id BIGINT(20),
  constraint pk_product primary key (id)
);

CREATE TABLE fyndiq_product (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  fyndiq_id BIGINT(20),
  product_id BIGINT(20),
  moms_percent INTEGER NOT NULL,
  is_blocked_by_fyndiq Boolean NOT NULL,
  product_state varchar(255) NOT NULL,
  price varchar(255) NOT NULL,
  oldprice varchar(255) NOT NULL,
  url varchar(255) NOT NULL,
  resource_uri varchar(255) NOT NULL,
  constraint pk_fyndiq_product primary key (id)
);

CREATE TABLE image (
  id BIGINT(20) AUTO_INCREMENT,
  product_id INTEGER,
  url VARCHAR(255),
  constraint pk_image primary key (id)
);

create sequence company_seq start with 1000;
create sequence product_seq start with 1000;
create sequence fyndiq_product_seq start with 1000;
create sequence image_seq start with 1000;

alter table image add constraint fk_image_product_1 foreign key (product_id) references product (id);
alter table fyndiq_product add constraint fk_fyndiq_product_1 foreign key (product_id) references product (id);
alter table product add constraint fk_company_1 foreign key (company_id) references company (id);
create index ix_product_image_1 on image (product_id);


# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists Product, Image, fyndiq_product, company;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists company_seq;
drop sequence if exists product_seq;
drop sequence if exists fyndiq_product_seq;
drop sequence if exists computer_seq;


# --- !Downs