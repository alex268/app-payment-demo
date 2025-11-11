CREATE TABLE account (
    account_id Int64 NOT NULL,
    bic Text NOT NULL,

    PRIMARY KEY(account_id)
) WITH (
    AUTO_PARTITIONING_BY_SIZE = DISABLED,
    AUTO_PARTITIONING_BY_LOAD = DISABLED,
    AUTO_PARTITIONING_MIN_PARTITIONS_COUNT = 1,
    AUTO_PARTITIONING_MAX_PARTITIONS_COUNT = 1
);

CREATE TABLE saldo (
    account_id Int64 NOT NULL,
    part_num Int32 NOT NULL,

    updated_ts Timestamp,
    amount Decimal(22, 9),

    PRIMARY KEY(account_id, part_num)
) WITH (
    AUTO_PARTITIONING_BY_SIZE = DISABLED,
    AUTO_PARTITIONING_BY_LOAD = DISABLED,
    AUTO_PARTITIONING_MIN_PARTITIONS_COUNT = 1,
    AUTO_PARTITIONING_MAX_PARTITIONS_COUNT = 1
);

CREATE TABLE account_config (
    account_id Int64 NOT NULL,
    weight Int32,

    PRIMARY KEY(account_id)
);

CREATE TABLE saldo_config (
    account_id Int64 NOT NULL,
    part_num Int32 NOT NULL,
    processor Text,

    PRIMARY KEY(account_id, part_num)
);

CREATE TABLE transaction (
    tx_id Text NOT NULL,

    acc_a Int64 NOT NULL,
    acc_b Int64 NOT NULL,
    acc_part_a Int32 NOT NULL,
    acc_part_b Int32 NOT NULL,

    amount Decimal(22,9),

    input_ts Timestamp,
    accepted_ts Timestamp,
    processed_ts Timestamp,

    PRIMARY KEY(acc_a, acc_part_a, tx_id)
) WITH (
    AUTO_PARTITIONING_BY_SIZE = ENABLED,
    AUTO_PARTITIONING_BY_LOAD = ENABLED,
    AUTO_PARTITIONING_PARTITION_SIZE_MB = 2000,
    AUTO_PARTITIONING_MIN_PARTITIONS_COUNT = 100,
    AUTO_PARTITIONING_MAX_PARTITIONS_COUNT = 200,
    PARTITION_AT_KEYS = (2, 3, 4, 5, 6, 25, 44, 63, 82)
);

CREATE TABLE saldo_update (
    account_id Int64 NOT NULL,
    part_num Int32 NOT NULL,

    created_ts Timestamp,
    tx_id String NOT NULL,

    amount Decimal(22,9),
    PRIMARY KEY(account_id, part_num, created_ts, tx_id)
) WITH (
    AUTO_PARTITIONING_BY_SIZE = ENABLED,
    AUTO_PARTITIONING_BY_LOAD = ENABLED,
    AUTO_PARTITIONING_PARTITION_SIZE_MB = 2000,
    AUTO_PARTITIONING_MIN_PARTITIONS_COUNT = 100,
    AUTO_PARTITIONING_MAX_PARTITIONS_COUNT = 200,
    PARTITION_AT_KEYS = (2, 3, 4, 5, 6, 25, 44, 63, 82)
);
