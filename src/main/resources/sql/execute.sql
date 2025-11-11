DECLARE $batch AS List<Struct<
  p1: Text,
  p2: Int64,
  p3: Int64,
  p4: Int32,
  p5: Int32,
  p6: Decimal(22, 9),
  p7: Timestamp,
  p8: Timestamp,
  p9: Timestamp
>>;

INSERT INTO transaction SELECT
  p1 AS tx_id,
  p2 AS acc_a,
  p3 AS acc_b,
  p4 AS acc_part_a,
  p5 AS acc_part_b,
  p6 AS amount,
  p7 AS input_ts,
  p8 AS accepted_ts,
  p9 AS processed_ts
FROM AS_TABLE($batch);

INSERT INTO saldo_update SELECT
  p3 AS account_id,
  p5 AS part_num,
  p9 AS created_ts,
  p1 AS tx_id,
  p6 AS amount
FROM AS_TABLE($batch);
