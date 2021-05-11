CREATE OR REPLACE FUNCTION gen_random_uuid()
    RETURNS uuid
    LANGUAGE sql
AS
$function$
SELECT md5(random()::text || clock_timestamp()::text)::uuid
$function$
;

CREATE TABLE IF NOT EXISTS events
(
    id                  UUID primary key default gen_random_uuid(),
    operation           varchar(100) not null,
    consumer            varchar(100) not null,
    kafka_partition     int not null,
    kafka_offset        int not null,
    event_envelope      JSON not null,
    createdAt           timestamp not null default NOW(),
    updatedAt           timestamp not null default NOW()
);



CREATE OR REPLACE FUNCTION notify_events()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
DECLARE
BEGIN
  PERFORM pg_notify('events_queue', row_to_json(NEW)::text);
RETURN NEW;
END;
$function$
;

CREATE TRIGGER notify_events_events
    AFTER INSERT ON events
    FOR EACH ROW
    EXECUTE PROCEDURE notify_events();