--liquibase formatted sql

--changeset bunny:1487849040814-1 dbms:postgresql
CREATE TYPE backend_status AS ENUM (
    'ACTIVE',
    'INACTIVE'
);
--rollback DROP TYPE backend_status;

--changeset bunny:1487849040814-2 dbms:postgresql
CREATE TYPE backend_type AS ENUM (
    'LOCAL',
    'ACTIVE_MQ',
    'RABBIT_MQ'
);
--rollback DROP TYPE backend_type;

--changeset bunny:1487849040814-3 dbms:postgresql
CREATE TYPE context_record_status AS ENUM (
    'RUNNING',
    'COMPLETED',
    'FAILED',
    'ABORTED'
);
--rollback DROP TYPE context_record_status;

--changeset bunny:1487849040814-4 dbms:postgresql
CREATE TYPE event_status AS ENUM (
    'PROCESSED',
    'UNPROCESSED',
    'FAILED'
);
--rollback DROP TYPE event_status;

--changeset bunny:1487849040814-5 dbms:postgresql
CREATE TYPE job_record_state AS ENUM (
    'PENDING',
    'READY',
    'RUNNING',
    'COMPLETED',
    'FAILED',
    'ABORTED'
);
--rollback DROP TYPE job_record_state;

--changeset bunny:1487849040814-6 dbms:postgresql
CREATE TYPE job_status AS ENUM (
    'PENDING',
    'READY',
    'STARTED',
    'ABORTED',
    'FAILED',
    'COMPLETED',
    'RUNNING'
);
--rollback DROP TYPE job_status;

--changeset bunny:1487849040814-7 dbms:postgresql
CREATE TYPE link_merge_type AS ENUM (
    'merge_nested',
    'merge_flattened'
);
--rollback DROP TYPE link_merge_type;

--changeset bunny:1487849040814-8 dbms:postgresql
CREATE TYPE persistent_event_type AS ENUM (
    'INIT',
    'JOB_STATUS_UPDATE_RUNNING',
    'JOB_STATUS_UPDATE_COMPLETED'
);
--rollback DROP TYPE persistent_event_type;

--changeset bunny:1487849040814-9 dbms:postgresql
CREATE TYPE port_type AS ENUM (
    'INPUT',
    'OUTPUT'
);
--rollback DROP TYPE port_type;

--changeset bunny:1487849040814-10 dbms:postgresql
CREATE TABLE application (
    hash text NOT NULL,
    app text,
    created_at timestamp NOT NULL DEFAULT now(),
    modified_at timestamp NOT NULL DEFAULT now()
);
--rollback DROP TABLE application;

--changeset bunny:1487849040814-11 dbms:postgresql
CREATE TABLE backend (
    id uuid NOT NULL,
    name text,
    type backend_type,
    heartbeat_info timestamp without time zone NOT NULL,
    status backend_status NOT NULL,
    configuration jsonb
);
--rollback DROP TABLE backend;

--changeset bunny:1487849040814-12 dbms:postgresql
CREATE TABLE context_record (
    id uuid NOT NULL,
    status context_record_status NOT NULL,
    config jsonb,
    created_at timestamp NOT NULL DEFAULT now(),
    modified_at timestamp NOT NULL DEFAULT now()
);
--rollback DROP TABLE context_record;

--changeset bunny:1487849040814-13 dbms:postgresql
CREATE TABLE dag_node (
    id uuid NOT NULL,
    dag jsonb,
    created_at timestamp NOT NULL DEFAULT now(),
    modified_at timestamp NOT NULL DEFAULT now()
);
--rollback DROP TABLE dag_node;

--changeset bunny:1487849040814-14 dbms:postgresql
CREATE TABLE event (
    id uuid NOT NULL,
    type persistent_event_type,
    status event_status NOT NULL,
    event jsonb,
    created_at timestamp NOT NULL DEFAULT now(),
    modified_at timestamp NOT NULL DEFAULT now()
);
--rollback DROP TABLE event;

--changeset bunny:1487849040814-15 dbms:postgresql
CREATE TABLE job (
    id uuid NOT NULL,
    root_id uuid,
    name text NOT NULL,
    parent_id uuid,
    status job_status NOT NULL,
    message text,
    inputs jsonb,
    outputs jsonb,
    resources jsonb,
    group_id uuid,
    produced_by_node  text,
    backend_id uuid,
    app text,
    config jsonb,
    created_at timestamp NOT NULL DEFAULT now(),
    modified_at timestamp NOT NULL DEFAULT now(),
    CONSTRAINT job_backend_status_check CHECK (((backend_id IS NOT NULL) OR (status <> 'RUNNING'::job_status) OR (parent_id IS NULL)))
);
--rollback DROP TABLE job;

--changeset bunny:1487849040814-16 dbms:postgresql
CREATE TABLE job_record (
    external_id uuid NOT NULL,
    id text NOT NULL,
    root_id uuid,
    parent_id uuid,
    blocking boolean NOT NULL,
    job_state job_record_state NOT NULL,
    input_counters jsonb NOT NULL,
    output_counters jsonb NOT NULL,
    is_scattered boolean NOT NULL,
    is_container boolean NOT NULL,
    is_scatter_wrapper boolean NOT NULL,
    global_inputs_count integer NOT NULL,
    global_outputs_count integer NOT NULL,
    scatter_strategy jsonb,
    dag_hash text,
    created_at timestamp NOT NULL DEFAULT now(),
    modified_at timestamp NOT NULL DEFAULT now()
);
--rollback DROP TABLE job_record;

--changeset bunny:1487849040814-17 dbms:postgresql
CREATE TABLE link_record (
    context_id uuid,
    source_job_id text NOT NULL,
    source_job_port_id text NOT NULL,
    source_type port_type NOT NULL,
    destination_job_id text NOT NULL,
    destination_job_port_id text NOT NULL,
    destination_type port_type NOT NULL,
    "position" integer NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    modified_at timestamp NOT NULL DEFAULT now()
);
--rollback DROP TABLE link_record;

--changeset bunny:1487849040814-18 dbms:postgresql
CREATE TABLE variable_record (
    job_id text NOT NULL,
    value jsonb,
    port_id text NOT NULL,
    type port_type NOT NULL,
    link_merge link_merge_type NOT NULL,
    is_wrapped boolean NOT NULL,
    globals_count integer NOT NULL,
    times_updated_count integer NOT NULL,
    context_id uuid,
    is_default boolean NOT NULL,
    transform jsonb,
    created_at timestamp NOT NULL DEFAULT now(),
    modified_at timestamp NOT NULL DEFAULT now()
);
--rollback DROP TABLE variable_record;

--changeset bunny:1487849040814-19 dbms:postgresql
ALTER TABLE ONLY application
    ADD CONSTRAINT application_pkey PRIMARY KEY (hash);
--rollback ALTER TABLE ONLY application DROP CONSTRAINT application_pkey;

--changeset bunny:1487849040814-20 dbms:postgresql
ALTER TABLE ONLY backend
    ADD CONSTRAINT backend_pkey PRIMARY KEY (id);
--rollback ALTER TABLE ONLY backend DROP CONSTRAINT backend_pkey;

--changeset bunny:1487849040814-21 dbms:postgresql
ALTER TABLE ONLY context_record
    ADD CONSTRAINT context_record_pkey PRIMARY KEY (id);
--rollback ALTER TABLE ONLY context_record DROP CONSTRAINT context_record_pkey;

--changeset bunny:1487849040814-22 dbms:postgresql
ALTER TABLE ONLY dag_node
    ADD CONSTRAINT dag_node_pkey PRIMARY KEY (id);
--rollback ALTER TABLE ONLY dag_node DROP CONSTRAINT dag_node_pkey;

--changeset bunny:1487849040814-23 dbms:postgresql
ALTER TABLE ONLY job
    ADD CONSTRAINT job_pkey PRIMARY KEY (id);
--rollback ALTER TABLE ONLY job DROP CONSTRAINT job_pkey;

--changeset bunny:1487849040814-24 dbms:postgresql
ALTER TABLE ONLY job_record
    ADD CONSTRAINT job_record_pkey PRIMARY KEY (external_id);
--rollback ALTER TABLE ONLY job_record DROP CONSTRAINT job_record_pkey;

--changeset bunny:1487849040814-25 dbms:postgresql
CREATE INDEX application_id_index ON application USING btree (hash);
--rollback DROP INDEX application_id_index;

--changeset bunny:1487849040814-26 dbms:postgresql
CREATE INDEX backend_id_index ON backend USING btree (id);
--rollback DROP INDEX backend_id_index;

--changeset bunny:1487849040814-27 dbms:postgresql
CREATE INDEX backend_status_index ON backend USING btree (status);
--rollback DROP INDEX backend_status_index;

--changeset bunny:1487849040814-28 dbms:postgresql
CREATE INDEX context_record_id_index ON context_record USING btree (id);
--rollback DROP INDEX context_record_id_index;

--changeset bunny:1487849040814-29 dbms:postgresql
CREATE INDEX context_record_status_index ON context_record USING btree (status);
--rollback DROP INDEX context_record_status_index;

--changeset bunny:1487849040814-30 dbms:postgresql
CREATE UNIQUE INDEX event_id_type_index ON event USING btree (id, type);
--rollback DROP INDEX event_id_type_index;

--changeset bunny:1487849040814-33 dbms:postgresql
CREATE INDEX job_backend_status_index ON job USING btree (backend_id, status);
--rollback DROP INDEX job_backend_status_index;

--changeset bunny:1487849040814-36 dbms:postgresql
CREATE INDEX job_id_index ON job USING btree (id);
--rollback DROP INDEX job_id_index;

--changeset bunny:1487849040814-38 dbms:postgresql
CREATE UNIQUE INDEX job_record_name_index ON job_record USING btree (root_id, id);
--rollback DROP INDEX job_record_name_index;

--changeset bunny:1487849040814-39 dbms:postgresql
CREATE INDEX job_record_parent_index ON job_record USING btree (root_id, parent_id);
--rollback DROP INDEX job_record_parent_index;

--changeset bunny:1487849040814-40 dbms:postgresql
CREATE INDEX job_record_root_index ON job_record USING btree (root_id);
--rollback DROP INDEX job_record_root_index;

--changeset bunny:1487849040814-41 dbms:postgresql
CREATE INDEX job_record_state_index ON job_record USING btree (root_id, job_state);
--rollback DROP INDEX job_record_state_index;

--changeset bunny:1487849040814-42 dbms:postgresql
CREATE INDEX job_root_index ON job USING btree (root_id);
--rollback DROP INDEX job_root_index;

--changeset bunny:1487849040814-44 dbms:postgresql
CREATE INDEX job_status_index ON job USING btree (status);
--rollback DROP INDEX job_status_index;

--changeset bunny:1487849040814-45 dbms:postgresql
CREATE INDEX link_record_context_index ON link_record USING btree (context_id);
--rollback DROP INDEX link_record_context_index;

--changeset bunny:1487849040814-46 dbms:postgresql
CREATE INDEX link_record_destination_job_index ON link_record USING btree (context_id, destination_job_id);
--rollback DROP INDEX link_record_destination_job_index;

--changeset bunny:1487849040814-49 dbms:postgresql
CREATE INDEX link_record_source_job_index ON link_record USING btree (context_id, source_job_id);
--rollback DROP INDEX link_record_source_job_index;

--changeset bunny:1487849040814-54 dbms:postgresql
CREATE INDEX variable_record_job_index ON variable_record USING btree (job_id, context_id);
--rollback DROP INDEX variable_record_job_index;

--changeset bunny:1487849040814-57 dbms:postgresql
ALTER TABLE ONLY job
    ADD CONSTRAINT job_backend_id_fkey FOREIGN KEY (backend_id) REFERENCES backend(id) ON DELETE SET NULL;
--rollback ALTER TABLE ONLY job DROP CONSTRAINT job_backend_id_fkey;

--changeset bunny:1487849040814-58 dbms:postgresql
ALTER TABLE ONLY job_record
    ADD CONSTRAINT job_record_root_id_fkey FOREIGN KEY (root_id) REFERENCES context_record(id) ON DELETE CASCADE;
--rollback ALTER TABLE ONLY job_record DROP CONSTRAINT job_record_root_id_fkey;

--changeset bunny:1487849040814-59 dbms:postgresql
ALTER TABLE ONLY link_record
    ADD CONSTRAINT link_record_context_id_fkey FOREIGN KEY (context_id) REFERENCES context_record(id) ON DELETE CASCADE;
--rollback ALTER TABLE ONLY link_record DROP CONSTRAINT link_record_context_id_fkey;

--changeset bunny:1487849040814-60 dbms:postgresql
ALTER TABLE ONLY link_record
    ADD CONSTRAINT link_record_destination_job_id_fkey FOREIGN KEY (destination_job_id, context_id) REFERENCES job_record(id, root_id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
--rollback ALTER TABLE ONLY link_record DROP CONSTRAINT link_record_destination_job_id_fkey;

--changeset bunny:1487849040814-61 dbms:postgresql
ALTER TABLE ONLY link_record
    ADD CONSTRAINT link_record_source_job_id_fkey FOREIGN KEY (source_job_id, context_id) REFERENCES job_record(id, root_id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
--rollback ALTER TABLE ONLY link_record DROP CONSTRAINT link_record_source_job_id_fkey;

--changeset bunny:1487849040814-62 dbms:postgresql
ALTER TABLE ONLY variable_record
    ADD CONSTRAINT variable_record_context_id_fkey FOREIGN KEY (context_id) REFERENCES context_record(id) ON DELETE CASCADE;
--rollback ALTER TABLE ONLY variable_record DROP CONSTRAINT variable_record_context_id_fkey;

--changeset bunny:1487849040814-63 dbms:postgresql
ALTER TABLE ONLY variable_record
    ADD CONSTRAINT variable_record_job_id_fkey FOREIGN KEY (job_id, context_id) REFERENCES job_record(id, root_id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
--rollback ALTER TABLE ONLY variable_record DROP CONSTRAINT variable_record_job_id_fkey;

--changeset bunny:1487849040814-64 dbms:postgresql
CREATE TABLE job_stats (
    root_id	    uuid NOT NULL,
    completed   integer NOT NULL,
    running     integer NOT NULL,
    total       integer NOT NULL
);
--rollback DROP TABLE job_stats

--changeset bunny:1487849040814-65 dbms:postgresql
ALTER TABLE ONLY job_stats
    ADD CONSTRAINT job_stats_pkey PRIMARY KEY (root_id);
--rollback ALTER TABLE ONLY job_stats DROP CONSTRAINT job_stats_pkey;

--changeset bunny:1487849040814-66 dbms:postgresql
ALTER TABLE ONLY job_stats
    ADD CONSTRAINT job_stats_id_fkey FOREIGN KEY (root_id) REFERENCES job(id) ON DELETE CASCADE;
--rollback ALTER TABLE ONLY job_stats DROP CONSTRAINT job_stats_id_fkey;

--changeset bunny:1487849040814-67 dbms:postgresql
CREATE TABLE intermediary_files (
    root_id   uuid  NOT NULL,
    filename  text NOT NULL,
    count     integer NOT NULL
);
--rollback DROP TABLE intermediary_files

--changeset bunny:1487849040814-68 dbms:postgresql
ALTER TABLE event ADD COLUMN message text;
--rollback ALTER TABLE event drop column message;

--changeset bunny:1487849040814-69 dbms:postgresql
ALTER TABLE event drop column type;
--rollback ALTER TABLE event ADD COLUMN type persistent_event_type;

--changeset bunny:1487849040814-70 dbms:postgresql
alter table job alter column outputs  type bytea using convert_to(outputs::text,'UTF8');
alter table job alter column inputs  type bytea using convert_to(inputs::text,'UTF8');
alter table event alter column event  type bytea using convert_to(event::text,'UTF8');
alter table variable_record alter column value  type bytea using convert_to(value::text,'UTF8');


--changeset bunny:1487849040814-71 dbms:postgresql
alter table backend alter column configuration type text
--rollback alter table backend alter column configuration type jsonb USING configuration::jsonb


--changeset bunny:1487849040814-72 dbms:postgresql
ALTER TABLE intermediary_files ADD CONSTRAINT key PRIMARY KEY (root_id, filename);
--rollback ALTER TABLE public.intermediary_files DROP CONSTRAINT key PRIMARY KEY (root_id, filename);

--changeset bunny:1487849040814-73 dbms:postgresql
ALTER TABLE public.job drop CONSTRAINT job_backend_status_check;
--rollback ALTER TABLE job ADD CONSTRAINT job_backend_status_check CHECK (backend_id IS NOT NULL OR status <> 'RUNNING'::job_status OR parent_id IS NULL);

--changeset bunny:1487849040814-74 dbms:postgresql
ALTER TABLE job_stats
    DROP CONSTRAINT job_stats_id_fkey;
ALTER TABLE job_stats ADD CONSTRAINT job_stats_context_fkey FOREIGN KEY (root_id) REFERENCES context_record (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE;
--rollback ALTER TABLE job_stats DROP CONSTRAINT job_stats_context_fkey; ALTER TABLE job_stats ADD CONSTRAINT job_stats_id_fkey FOREIGN KEY (root_id) REFERENCES job (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE;
