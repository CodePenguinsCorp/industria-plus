create table if not exists bootstrap_registry (
    id bigint primary key auto_increment,
    created_at timestamp not null default current_timestamp,
    note varchar(255) not null
);

insert into bootstrap_registry (note)
select 'Projeto inicializado'
where not exists (
    select 1
    from bootstrap_registry
    where note = 'Projeto inicializado'
);
