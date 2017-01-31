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
    scatter_method scatter_methids NOT NULL,
    inputs jsonb,
    outputs jsonb,
    default_values jsonb
);

--changeset luka:4 dbms:postgres

CREATE TABLE job_backend (
    job_id  text,
    root_id text,
    backend_id text
);

--changeset luka:5 dbms:postgres

--changeset luka:6 dbms:postgres

--changeset luka:7 dbms:postgres

--changeset luka:8 dbms:postgres

--changeset luka:9 dbms:postgres