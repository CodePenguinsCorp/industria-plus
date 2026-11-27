-- V4: técnicos passam a ser usuários (app_users com role TECHNICIAN).
-- A tabela technicians é removida e seus registros são migrados sem perda:
--   * técnicos que já possuíam acesso reaproveitam o usuário vinculado;
--   * técnicos sem acesso viram usuários TECHNICIAN sem senha utilizável
--     (o administrador define uma senha provisória em Usuários);
--   * chamados e histórico passam a apontar para o id do usuário.
-- Também adiciona o gestor vinculado ao técnico e a troca obrigatória de senha.

alter table app_users add column specialty varchar(120);
alter table app_users add column manager_id bigint;
alter table app_users add column must_change_password boolean not null default false;

-- 1) Especialidade dos técnicos que já possuíam acesso.
update app_users u
   set specialty = (select t.specialty from technicians t where t.id = u.technician_id)
 where u.technician_id is not null;

-- 2) Técnicos sem acesso viram usuários TECHNICIAN.
--    Se o e-mail já pertencer a outro usuário, um e-mail técnico único é gerado.
insert into app_users (name, email, password_hash, role, technician_id, specialty,
                       must_change_password, created_at)
select t.name,
       case
           when exists (select 1 from app_users e where lower(e.email) = lower(t.email))
               then concat('tecnico-', t.id, '@migracao.invalid')
           else lower(t.email)
       end,
       'MIGRATED-WITHOUT-PASSWORD',
       'TECHNICIAN',
       t.id,
       t.specialty,
       true,
       t.created_at
  from technicians t
 where not exists (select 1 from app_users l where l.technician_id = t.id);

-- 3) Chamados passam a referenciar app_users.
alter table maintenance_requests drop constraint fk_maintenance_requests_technician;

update maintenance_requests r
   set technician_id = (select u.id from app_users u where u.technician_id = r.technician_id)
 where r.technician_id is not null;

alter table maintenance_requests
    add constraint fk_maintenance_requests_technician
        foreign key (technician_id) references app_users (id);

-- 4) Histórico: ids antigos de técnico passam a ser ids de usuário.
--    Técnicos já excluídos não têm correspondência; o nome registrado é preservado.
update maintenance_history h
   set previous_technician_id = (select u.id from app_users u
                                  where u.technician_id = h.previous_technician_id)
 where h.previous_technician_id is not null;

update maintenance_history h
   set current_technician_id = (select u.id from app_users u
                                 where u.technician_id = h.current_technician_id)
 where h.current_technician_id is not null;

-- 5) Remove o vínculo antigo e a tabela technicians.
alter table app_users drop constraint fk_app_users_technician;
alter table app_users drop constraint chk_app_users_technician;
alter table app_users drop column technician_id;

drop table technicians;

-- 6) Gestor vinculado: somente técnicos podem ter gestor.
alter table app_users
    add constraint fk_app_users_manager foreign key (manager_id) references app_users (id);
alter table app_users
    add constraint chk_app_users_manager check (manager_id is null or role = 'TECHNICIAN');

create index idx_app_users_role on app_users (role, name);
