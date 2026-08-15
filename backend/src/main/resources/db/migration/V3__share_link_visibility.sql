ALTER TABLE share_link
    ADD COLUMN visible_to_invitee TINYINT(1) NOT NULL DEFAULT 0;
