CREATE DATABASE db1;
CREATE USER db1_user WITH PASSWORD 'db1_pass';
GRANT ALL PRIVILEGES ON DATABASE "db1" to db1_user;

CREATE DATABASE db2;
CREATE USER db2_user WITH PASSWORD 'db2_pass';
GRANT ALL PRIVILEGES ON DATABASE "db2" to db2_user;

