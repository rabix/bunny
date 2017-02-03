CREATE TABLE APPLICATION (
	id	    uuid primary key,
    app     bytea
);

CREATE INDEX application_id_index ON application (id);

CREATE TYPE backend_type as enum ('LOCAL', 'ACTIVE_MQ', 'RABBIT_MQ');

CREATE TABLE backend (
	id				uuid primary key
	type            backend_type not null,
    configuration 	jsonb
);

CREATE INDEX backend_id_index ON backend (id);
CREATE INDEX backend_name_index ON backend (name);


CREATE TYPE root_job_status AS ENUM('RUNNING', 'COMPLETED', 'FAILED');

CREATE TABLE root_job (
    id          uuid primary key,
    status      root_job_status not null,
    config      jsonb
);

create index root_job_id_index on root_job(id);
create index root_job_status_index on root_job(status);



CREATE TYPE job_status as enum ('PENDING', 'READY', 'STARTED', 'ABORTED', 'FAILED', 'COMPLETED', 'RUNNING');
CREATE TYPE resources as (
    cpu             bigint not null,
    mem_mb          bigint not null,
    disk_space_mb   bigint not null,
    network_access  boolean not null,
    working_dir     text not null,
    tmp_dir         text  not null,
    out_dir_size    bigint not null,
    tmp_dir_size    bigint not null
);

CREATE TABLE job (
    id			    uuid primary key,
    root_id 	    uuid references root_job,
    name            text not null,
    parent_id       uuid not null,
    status          job_status not null,
    message         text,
    inputs          jsonb,
    outputs         jsonb,
    resources       resources,
    group_id	    uuid,
    backend_id      uuid references backend on delete SET NULL,
    visible_ports   text[]
);

create index job_id_index on job(id);
create index job_parents_index on job(parents);
create index job_status_index on job(status);
create index job_root_index on job(root_id);
create unique index job_root_name_index on job(root_id, name);
create index job_group_index on job(group_id);
create index job_backend_index on job(backend_id);
create index job_backend_status_index on job(backend_id, status);
create index job_backend_status_root_index on job(backend_id, status, root_id);


CREATE TYPE job_record_state as ENUM ('PENDING', 'READY', 'RUNNING', 'COMPLETED', 'FAILED');

CREATE TABLE job_record (
    id				        uuid primary key,
    name					text not null,
    root_id					uuid references root_job,
    parent_id				uuid,
    blocking    			boolean, not null
    job_state				job_record_state not null,
    input_counters			jsonb not null,
    output_counters			jsonb not null,
    is_scattered			boolean not null,
    is_container			boolean not null,
    is_scatter_wrapper		boolean not null,
    global_inputs_count		integer not null,
    global_outputs_count	integer not null,
    scatter_strategy		jsonb
);

CREATE UNIQUE INDEX job_record_name_index on job_record (root_id, name);
CREATE INDEX job_record_root_index on job_record (root_id);
CREATE INDEX job_record_parent_index on job_record (root_id, parent_id);
CREATE INDEX job_record_state_index on job_record (root_id, job_state);

CREATE TABLE dag_node (
	root_id		uuid references root_job,
    dag 	    jsonb
);

CREATE TYPE port_type as ENUM ('INPUT', 'OUTPUT');

CREATE TABLE link_record (
    root_id				    uuid references root_job,
    source_job_name			text not null,
    source_job_port		    text not null,
    source_type				port_type not null,
    destination_job_name	text not null,
    destination_job_port	text not null,
    destination_type		port_type not null,
    position				integer not null
);

CREATE UNIQUE INDEX link_record_index on link_record (root_id, source_job_name, source_job_port, source_type, destination_job_name, destination_job_port, destination_type);
CREATE INDEX link_record_source_index on link_record (root_id, source_job_name, source_job_port);
CREATE INDEX link_record_source_job_index on link_record (root_id, source_job_name);
CREATE INDEX link_record_source_type_index on link_record (root_id, source_job_name, source_type);
CREATE INDEX link_record_source_port_destination_type_index on link_record (root_id, source_job_name, source_job_port, destination_type);

Create TYPE link_merge_type as ENUM ('merge_nested', 'merge_flattened')

CREATE TABLE variable_record (
    job_name			text not null,
    value				jsonb,
    port_id				text not null,
    type				port_type not null,
    link_merge			link_merge_type not null,
    is_wrapped			boolean not null,
    globals_count		integer not null,
    times_updated_count	integer not null,
    root_id 			uuid references root_job,
    is_default			boolean not null,
    transform			jsonb
);

CREATE UNIQUE INDEX variable_record_index on variable_record (job_id, port_id, type, root_id);
CREATE INDEX variable_record_type_index on variable_record (job_id, type, root_id);
CREATE INDEX variable_record_port_index on variable_record (job_id, port_id, root_id);
