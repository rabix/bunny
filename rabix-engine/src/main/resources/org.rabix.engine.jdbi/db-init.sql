--liquibase formatted sql

--changeset luka:1 dbms:postgres

CREATE TABLE application (
    id  text PRIMARY KEY,
    app text NOT NULL
);

--rollback DROP TABLE application;

--changeset luka:2 dbms:postgres

CREATE TABLE backend (
    id  text PRIMARY KEY,
    app text NOT NULL
);

--changeset luka:3 dbms:postgres

CREATE TYPE scatter_methods AS ENUM ('EXECUTABLE', 'CONTAINER');

CREATE TABLE dag_node (
    id  text PRIMARY KEY,
    app text NOT NULL references application,
    scatter_method scatter_methods NOT NULL,
    inputs jsonb,
    outputs jsonb,
    default_values jsonb
);

--rollback DROP TABLE dag_node; DROP TYPE scatter_methods;

--changeset luka:4 dbms:postgres

CREATE TABLE job_backend (
    job_id  text,
    root_id text,
    backend_id text
);

--changeset luka:5 dbms:postgres

DROP TABLE job_record;

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

--changeset luka:6 dbms:postgres

--changeset luka:7 dbms:postgres

--rollback DROP TABLE job;

CREATE TABLE job (
    id			text,
    root_id 	text,
    job			jsonb,
    group_id	text
);

--changeset luka:8 dbms:postgres

--rollback DROP TABLE link_record;

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

--changeset luka:9 dbms:postgres

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

--rollback DROP TABLE variable_record;
