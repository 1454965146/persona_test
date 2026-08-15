UPDATE comparison
SET status = 'COMPLETED'
WHERE (status IS NULL OR status = '')
  AND analysis_content IS NOT NULL;

DELETE c1
FROM comparison c1
JOIN comparison c2
  ON c1.report_id_a = c2.report_id_a
 AND c1.report_id_b = c2.report_id_b
 AND c1.relationship_type = c2.relationship_type
 AND c1.id < c2.id;

ALTER TABLE comparison
    ADD UNIQUE KEY uk_comparison_pair_relationship (report_id_a, report_id_b, relationship_type),
    ADD KEY idx_comparison_owner_created (owner_user_id, created_at);

ALTER TABLE auth_token
    ADD KEY idx_auth_token_expiry_revoked (expires_at, revoked);

ALTER TABLE report
    ADD KEY idx_report_user_created (user_id, created_at);

ALTER TABLE share_link
    ADD KEY idx_share_link_status_expiry (status, expires_at);
