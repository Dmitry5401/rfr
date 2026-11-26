INSERT INTO friendship (id, requester_id, addressee_id, status, created_at, responded_at, version)
SELECT UUID_TO_BIN(UUID(), true),
       requester.id,
       addressee.id,
       :status,
       CURRENT_TIMESTAMP(6),
       CASE WHEN :status = 'ACCEPTED' THEN CURRENT_TIMESTAMP(6) ELSE NULL END,
       0
FROM `user` requester
CROSS JOIN `user` addressee
WHERE requester.username = :requester
  AND addressee.username = :addressee
