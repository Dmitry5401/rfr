SELECT COUNT(*)
FROM friendship f
JOIN `user` addressee ON addressee.id = f.addressee_id
WHERE addressee.username = :username
  AND f.status = 'PENDING'
