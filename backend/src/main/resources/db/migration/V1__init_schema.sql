CREATE TABLE app_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    openid VARCHAR(64) NULL,
    password_hash VARCHAR(200) NULL,
    username VARCHAR(64) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_user_openid (openid),
    UNIQUE KEY uk_app_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE question (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dimension VARCHAR(10) NOT NULL,
    question_text VARCHAR(500) NOT NULL,
    is_positive TINYINT(1) NOT NULL DEFAULT 1,
    sort_order INT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE test_session (
    id BIGINT NOT NULL AUTO_INCREMENT,
    answers_json TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    dimension_scores_json TEXT NULL,
    session_code VARCHAR(32) NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_id BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_test_session_session_code (session_code),
    KEY idx_test_session_user_id (user_id),
    CONSTRAINT fk_test_session_user FOREIGN KEY (user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE report (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    dimension_scores_json TEXT NULL,
    nickname VARCHAR(50) NULL,
    personality_type VARCHAR(10) NULL,
    report_code VARCHAR(32) NOT NULL,
    report_content MEDIUMTEXT NULL,
    session_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_report_report_code (report_code),
    KEY idx_report_session_id (session_id),
    KEY idx_report_user_id (user_id),
    CONSTRAINT fk_report_session FOREIGN KEY (session_id) REFERENCES test_session (id),
    CONSTRAINT fk_report_user FOREIGN KEY (user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE share_link (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    inviter_name VARCHAR(50) NULL,
    relationship_type VARCHAR(20) NULL,
    share_code VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    invitee_report_id BIGINT NULL,
    inviter_report_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_share_link_share_code (share_code),
    KEY idx_share_link_invitee_report (invitee_report_id),
    KEY idx_share_link_inviter_report (inviter_report_id),
    CONSTRAINT fk_share_link_inviter FOREIGN KEY (inviter_report_id) REFERENCES report (id),
    CONSTRAINT fk_share_link_invitee FOREIGN KEY (invitee_report_id) REFERENCES report (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comparison (
    id BIGINT NOT NULL AUTO_INCREMENT,
    analysis_content MEDIUMTEXT NULL,
    created_at DATETIME(6) NOT NULL,
    name_a VARCHAR(50) NULL,
    name_b VARCHAR(50) NULL,
    relationship_type VARCHAR(20) NULL,
    report_id_a BIGINT NOT NULL,
    report_id_b BIGINT NOT NULL,
    error_message TEXT NULL,
    status VARCHAR(20) NOT NULL,
    updated_at DATETIME(6) NULL,
    owner_user_id BIGINT NULL,
    PRIMARY KEY (id),
    KEY idx_comparison_report_a (report_id_a),
    KEY idx_comparison_report_b (report_id_b),
    KEY idx_comparison_owner_user (owner_user_id),
    CONSTRAINT fk_comparison_report_a FOREIGN KEY (report_id_a) REFERENCES report (id),
    CONSTRAINT fk_comparison_report_b FOREIGN KEY (report_id_b) REFERENCES report (id),
    CONSTRAINT fk_comparison_owner_user FOREIGN KEY (owner_user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE auth_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked BIT(1) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_token_token_hash (token_hash),
    KEY idx_auth_token_user_id (user_id),
    CONSTRAINT fk_auth_token_user FOREIGN KEY (user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
