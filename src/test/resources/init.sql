CREATE TABLE person
(
    id UInt32,
    name String,
    tags Array(String)
)
    ENGINE = MergeTree
ORDER BY id;

INSERT INTO person VALUES
                       (1, 'Alice', ['java', 'spring']),
                       (2, 'Bob',   ['docker', 'kotlin']),
                       (3, 'Carol', ['java', 'clickhouse']);