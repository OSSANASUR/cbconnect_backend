-- No-op : V202605010001 (qui aurait converti la colonne en NUMERIC) est désormais no-op,
-- donc cette migration de revert n'a plus rien à faire. La colonne reste VARCHAR(30).
SELECT 1;
