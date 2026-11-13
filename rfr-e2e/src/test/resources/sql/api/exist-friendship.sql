SELECT COUNT(*)
FROM friendship f
JOIN `user` requester ON requester.id = f.requester_id
JOIN `user` addressee ON addressee.id = f.addressee_id
WHERE requester.username = :requester
  AND addressee.username = :addressee
  AND f.status = 'ACCEPTED'
