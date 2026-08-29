create table sectors (
    id bigint not null auto_increment,
    name varchar(100) not null,
    description varchar(255),
    created_at timestamp not null default current_timestamp,
    primary key (id)
);

create table equipments (
    id bigint not null auto_increment,
    asset_tag varchar(50) not null,
    name varchar(120) not null,
    description varchar(500),
    sector_id bigint not null,
    created_at timestamp not null default current_timestamp,
    primary key (id),
    constraint uk_equipments_asset_tag unique (asset_tag),
    constraint fk_equipments_sector
        foreign key (sector_id) references sectors (id)
);

create table technicians (
    id bigint not null auto_increment,
    name varchar(120) not null,
    email varchar(254) not null,
    specialty varchar(120),
    created_at timestamp not null default current_timestamp,
    primary key (id),
    constraint uk_technicians_email unique (email)
);

create table maintenance_requests (
    id bigint not null auto_increment,
    title varchar(160) not null,
    description varchar(2000) not null,
    equipment_id bigint not null,
    sector_id bigint not null,
    technician_id bigint,
    request_type varchar(20) not null,
    urgency varchar(20) not null,
    status varchar(20) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    primary key (id),
    constraint chk_maintenance_requests_type
        check (request_type in ('PREVENTIVE', 'CORRECTIVE')),
    constraint chk_maintenance_requests_urgency
        check (urgency in ('LOW', 'MEDIUM', 'HIGH')),
    constraint chk_maintenance_requests_status
        check (status in ('OPEN', 'IN_PROGRESS', 'CLOSED')),
    constraint fk_maintenance_requests_equipment
        foreign key (equipment_id) references equipments (id),
    constraint fk_maintenance_requests_sector
        foreign key (sector_id) references sectors (id),
    constraint fk_maintenance_requests_technician
        foreign key (technician_id) references technicians (id)
);

create index idx_equipments_sector
    on equipments (sector_id);

create index idx_maintenance_requests_filter
    on maintenance_requests (status, urgency, created_at);

create index idx_maintenance_requests_technician_limit
    on maintenance_requests (technician_id, urgency, status);
