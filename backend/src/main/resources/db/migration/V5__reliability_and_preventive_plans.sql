alter table equipments add column observation_start date;
alter table equipments add column observation_end date;
alter table equipments add column operating_hours decimal(12,2);

create table preventive_plans (
    id bigint primary key auto_increment,
    equipment_id bigint not null unique,
    title varchar(160) not null,
    description varchar(2000) not null,
    interval_days integer not null,
    next_due_date date not null,
    enabled boolean not null default true,
    constraint fk_preventive_plan_equipment foreign key (equipment_id) references equipments(id) on delete cascade,
    constraint chk_preventive_interval check (interval_days between 1 and 3650)
);

alter table maintenance_requests add column preventive_plan_id bigint;
alter table maintenance_requests add column scheduled_for date;
alter table maintenance_requests add constraint fk_request_preventive_plan foreign key (preventive_plan_id) references preventive_plans(id);
alter table maintenance_requests add constraint uq_preventive_occurrence unique (preventive_plan_id, scheduled_for);
