SELECT COUNT(*)
FROM friendship
WHERE status = :status
  AND (
    requester_id = UUID_TO_BIN(:profileId, true)
    OR addressee_id = UUID_TO_BIN(:profileId, true)
  )
