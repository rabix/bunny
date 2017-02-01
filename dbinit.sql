--DROP TABLE job_record;

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

CREATE TYPE backend_type as enum ('LOCAL', 'ACTIVE_MQ', 'RABBIT_MQ');

DROP TABLE backend;

CREATE TABLE backend (
	id				uuid primary key,
	name            text not null unique,
	type            backend_type not null,
    configuration 	jsonb
);

CREATE INDEX backend_id_index ON backend (id);
CREATE INDEX backend_name_index ON backend (name);

--DROP TABLE APPLICATION;

CREATE TABLE APPLICATION (
	id	text,
    app jsonb
);

--DROP TABLE context_record;

CREATE TYPE context_status as enum ('RUNNING', 'COMPLETED', 'FAILED');

CREATE TABLE context_record (
    id		        uuid primary key,
    external_id     text unique not null,
    status	        context_status not null,
    config	        jsonb
    );

CREATE INDEX contect_id_index ON context_record (id);
CREATE INDEX contect_external_id_index ON context_record (external_id);

    
--DROP TABLE dag_node;

CREATE TABLE dag_node (
	id		text,
    dag 	jsonb
);

--DROP TABLE job_backend;

CREATE TABLE job_backend (
    job_id		text,
    root_id 	text,
    backend_id 	text
);

--DROP TABLE job;

CREATE TABLE job (
    id			uuid,
    root_id 	uuid references context_record,
    job			jsonb,
    group_id	text
);

--DROP TABLE link_record;

CREATE TABLE link_record (
    context_id				text,
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

