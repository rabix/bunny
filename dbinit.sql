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

CREATE TYPE job_status as enum ('PENDING', 'READY', 'STARTED', 'ABORTED', 'FAILED', 'COMPLETED', 'RUNNING');
CREATE TYPE resources as (
    cpu             bigint,
    mem_mb          bigint,
    disk_space_mb   bigint,
    network_access  boolean,
    working_dir     text,
    tmp_dir         text
    out_dir_size    bigint,
    tmp_dir_size    bigint
);

CREATE TABLE job (
    id			    uuid primary key,
    root_id 	    uuid references job,
    name            text not null,
    parent_id       uuid not null,
    status          job_status not null,
    message         text,
    inputs          jsonb,
    outputs         jsonb,
    resources       resources,
    group_id	    uuid,
    visible_ports   text[],
    config          jsonb
);

create index job_id_index on job(id);
create index job_parents_index on job(parents);
create index job_status_index on job(status);
create unique index job_root_name_index on job(root_id, name);


CREATE TABLE job_record (
    id						text,
    external_id				text,
    root_id					text,
    parent_id				text,
    blocking    			boolean,
    job_state				text,
    input_counters			jsonb,
    output_counters			jsonb,
    is_scattered			boolean,
    is_container			boolean,
    is_scatter_wrapper		boolean,
    global_inputs_count		integer,
    global_outputs_count	integer,
    scatter_strategy		jsonb
);


--DROP TABLE dag_node;

CREATE TABLE dag_node (
	id		text,
    dag 	jsonb
);


--DROP TABLE link_record;

CREATE TABLE link_record (
    root_id				    uuid,
    source_job_id			text,
    source_job_port_id		text,
    source_type				text,
    destination_job_id		text,
    destination_job_port_id	text,
    destination_type		text,
    position				integer
);

--DROP TABLE variable_record;

CREATE TABLE variable_record (
    job_id				text,
    value				jsonb,
    port_id				text,
    type				text,
    link_merge			text,
    is_wrapped			boolean,
    globals_count		integer,
    times_updated_count	integer,
    context_id			text,
    is_default			boolean,
    transform			jsonb
    );

