-- Application

CREATE TABLE APPLICATION (
	hash	text primary key,
    app     text
);

CREATE INDEX application_id_index ON application (hash);


-- Backend

CREATE TYPE backend_type as enum ('LOCAL', 'ACTIVE_MQ', 'RABBIT_MQ');
CREATE TYPE backend_status as enum ('ACTIVE', 'INACTIVE');

CREATE TABLE backend (
	id				uuid primary key,
	name            text,
	type            backend_type,
	heartbeat_info  timestamp not null,
	status          backend_status not null,
    configuration 	jsonb
);

CREATE INDEX backend_id_index ON backend (id);
CREATE INDEX backend_status_index ON backend (status);


-- Event

CREATE TYPE event_status as enum ('PROCESSED', 'UNPROCESSED');
CREATE TYPE persistent_event_type as enum ('INIT', 'JOB_STATUS_UPDATE_RUNNING', 'JOB_STATUS_UPDATE_COMPLETED');

CREATE TABLE event (
    id              uuid not null,
    type            persistent_event_type,
    status          event_status not null,
    event           jsonb
);

CREATE UNIQUE INDEX event_id_type_index on event(id, type);
CREATE INDEX event_status_index on event(status);

-- Root Job

CREATE TYPE context_record_status AS ENUM('RUNNING', 'COMPLETED', 'FAILED');

CREATE TABLE context_record (
    id          uuid primary key,
    status      context_record_status not null,
    config      jsonb
);

create index context_record_id_index on context_record(id);
create index context_record_status_index on context_record(status);


-- Job

CREATE TYPE job_status as enum ('PENDING', 'READY', 'STARTED', 'ABORTED', 'FAILED', 'COMPLETED', 'RUNNING');

CREATE TABLE job (
    id			    uuid primary key,
    root_id 	    uuid,
    name            text not null,
    parent_id       uuid,
    status          job_status not null,
    message         text,
    inputs          jsonb,
    outputs         jsonb,
    resources       jsonb,
    group_id	    uuid,
    backend_id      uuid references backend on delete SET NULL,
    app             text
);

create index job_id_index on job(id);
create index job_parent_index on job(parent_id);
create index job_status_index on job(status);
create index job_root_index on job(root_id);
create unique index job_root_name_index on job(root_id, name);
create index job_group_index on job(group_id);
create index job_backend_index on job(backend_id);
create index job_backend_status_index on job(backend_id, status);
create index job_backend_status_root_index on job(backend_id, status, root_id);


-- JobRecord

CREATE TYPE job_record_state as ENUM ('PENDING', 'READY', 'RUNNING', 'COMPLETED', 'FAILED');

CREATE TABLE job_record (
    external_id		        uuid primary key,
    id  					text not null,
    root_id					uuid references context_record on delete cascade,
    parent_id				uuid,
    blocking    			boolean not null,
    job_state				job_record_state not null,
    input_counters			jsonb not null,
    output_counters			jsonb not null,
    is_scattered			boolean not null,
    is_container			boolean not null,
    is_scatter_wrapper		boolean not null,
    global_inputs_count		integer not null,
    global_outputs_count	integer not null,
    scatter_strategy		jsonb,
    dag_hash                text
);

CREATE UNIQUE INDEX job_record_name_index on job_record (root_id, id);
CREATE INDEX job_record_root_index on job_record (root_id);
CREATE INDEX job_record_parent_index on job_record (root_id, parent_id);
CREATE INDEX job_record_state_index on job_record (root_id, job_state);


-- DagNode

CREATE TABLE dag_node (
	id		    uuid primary key, -- references context_record,
    dag 	    jsonb
);


-- LinkRecords

CREATE TYPE port_type as ENUM ('INPUT', 'OUTPUT');

CREATE TABLE link_record (
    context_id			    uuid references context_record on delete cascade,
    source_job_id			text not null,
    source_job_port_id		text not null,
    source_type				port_type not null,
    destination_job_id	    text not null,
    destination_job_port_id	text not null,
    destination_type		port_type not null,
    position				integer not null
);

CREATE UNIQUE INDEX link_record_index on link_record (context_id, source_job_id, source_job_port_id, source_type, destination_job_id, destination_job_port_id, destination_type);
CREATE INDEX link_record_source_index on link_record (context_id, source_job_id, source_job_port_id);
CREATE INDEX link_record_source_job_index on link_record (context_id, source_job_id);
CREATE INDEX link_record_source_type_index on link_record (context_id, source_job_id, source_type);
CREATE INDEX link_record_source_port_destination_type_index on link_record (context_id, source_job_id, source_job_port_id, destination_type);



-- VariableRecord

Create TYPE link_merge_type as ENUM ('merge_nested', 'merge_flattened');

CREATE TABLE variable_record (
    job_id			    text not null,
    value				jsonb,
    port_id				text not null,
    type				port_type not null,
    link_merge			link_merge_type not null,
    is_wrapped			boolean not null,
    globals_count		integer not null,
    times_updated_count	integer not null,
    context_id 			uuid references context_record on delete cascade,
    is_default			boolean not null,
    transform			jsonb
);

CREATE UNIQUE INDEX variable_record_index on variable_record (job_id, port_id, type, context_id);
CREATE INDEX variable_record_type_index on variable_record (job_id, type, context_id);
CREATE INDEX variable_record_port_index on variable_record (job_id, port_id, context_id);
