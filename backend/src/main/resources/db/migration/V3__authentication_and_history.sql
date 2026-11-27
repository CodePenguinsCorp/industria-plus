create table app_users (
 id bigint primary key auto_increment,
 name varchar(120) not null,
 email varchar(254) not null unique,
 password_hash varchar(100) not null,
 role varchar(20) not null,
 technician_id bigint unique,
 created_at timestamp not null default current_timestamp,
 constraint chk_app_users_role check (role in ('ADMIN', 'MANAGER', 'TECHNICIAN')),
 constraint chk_app_users_technician check ((role = 'TECHNICIAN' and technician_id is not null) or (role <> 'TECHNICIAN' and technician_id is null)),
 constraint fk_app_users_technician foreign key (technician_id) references technicians(id)
);
create table maintenance_history (
 id bigint primary key auto_increment,
 request_id bigint not null,
 event_type varchar(30) not null,
 actor varchar(254) not null,
 description varchar(2000) not null,
 previous_status varchar(20),
 current_status varchar(20),
 previous_technician_id bigint,
 previous_technician_name varchar(120),
 current_technician_id bigint,
 current_technician_name varchar(120),
 created_at timestamp not null default current_timestamp,
 constraint fk_history_request foreign key (request_id) references maintenance_requests(id)
);
create index idx_history_request on maintenance_history(request_id, created_at, id);
create table replaced_parts (
 id bigint primary key auto_increment,
 history_id bigint not null,
 name varchar(120) not null,
 quantity int not null,
 constraint chk_parts_quantity check (quantity > 0),
 constraint fk_parts_history foreign key (history_id) references maintenance_history(id)
);
create index idx_parts_history on replaced_parts(history_id);
