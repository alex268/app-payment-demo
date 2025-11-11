UPSERT INTO account SELECT * FROM AS_TABLE(ListMap(ListFromRange(1, 101), ($x) -> {
    RETURN <|account_id: $x, bic: 'TEST'u || CAST($x AS Text) |>;
}));

-- accounts with multisaldos
UPSERT INTO saldo (account_id, part_num, amount, updated_ts) VALUES
    (1, 1, Decimal("25000000.0", 22, 9), CurrentUtcTimestamp()),
    (1, 2, Decimal("25000000.0", 22, 9), CurrentUtcTimestamp()),
    (1, 3, Decimal("25000000.0", 22, 9), CurrentUtcTimestamp()),
    (1, 4, Decimal("25000000.0", 22, 9), CurrentUtcTimestamp()),
    (2, 1, Decimal("50000000.0", 22, 9), CurrentUtcTimestamp()),
    (2, 2, Decimal("50000000.0", 22, 9), CurrentUtcTimestamp());

UPSERT INTO saldo SELECT * FROM AS_TABLE(ListMap(ListFromRange(3, 101), ($x) -> {
    RETURN <|account_id: $x, part_num: 1, amount: Decimal("100000000.0", 22, 9), updated_ts: CurrentUtcTimestamp() |>;
}));

-- TOTAL WEIGHT = 19 * (40 + 25 + 10 + 5 + 5) + 95 * 3 = 1900
-- hot accounts
UPSERT INTO account_config (account_id, weight) VALUES
    (1, 40 * 19),
    (2, 25 * 19),
    (3, 10 * 19),
    (4, 5 * 19),
    (5, 5 * 19);

-- cold accounts - weight 3
UPSERT INTO account_config SELECT * FROM AS_TABLE(ListMap(ListFromRange(6, 101), ($x) -> {
    RETURN <|account_id: $x, weight: 3 |>;
}));

-- saldo processors processors
UPSERT INTO saldo_config (account_id, part_num, processor) VALUES
    (1, 1, 'hot1[1]'),
    (1, 2, 'hot1[2]'),
    (1, 3, 'hot1[3]'),
    (1, 4, 'hot1[4]'),
    (2, 1, 'hot2[1]'),
    (2, 2, 'hot2[2]'),
    (3, 1, 'hot3'),
    (4, 1, 'hot4'),
    (5, 1, 'hot5');

UPSERT INTO saldo_config SELECT * FROM AS_TABLE(ListMap(ListFromRange(6, 25), ($x) -> {
    RETURN <|account_id: $x, part_num: 1, processor: 'cold1'u |>;
}));
UPSERT INTO saldo_config SELECT * FROM AS_TABLE(ListMap(ListFromRange(25, 44), ($x) -> {
    RETURN <|account_id: $x, part_num: 1, processor: 'cold2'u |>;
}));
UPSERT INTO saldo_config SELECT * FROM AS_TABLE(ListMap(ListFromRange(44, 63), ($x) -> {
    RETURN <|account_id: $x, part_num: 1, processor: 'cold3'u |>;
}));
UPSERT INTO saldo_config SELECT * FROM AS_TABLE(ListMap(ListFromRange(63, 82), ($x) -> {
    RETURN <|account_id: $x, part_num: 1, processor: 'cold4'u |>;
}));
UPSERT INTO saldo_config SELECT * FROM AS_TABLE(ListMap(ListFromRange(82, 101), ($x) -> {
    RETURN <|account_id: $x, part_num: 1, processor: 'cold5'u |>;
}));
