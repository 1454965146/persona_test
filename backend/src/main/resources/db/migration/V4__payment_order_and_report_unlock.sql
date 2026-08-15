ALTER TABLE report
    ADD COLUMN premium_unlocked TINYINT(1) NOT NULL DEFAULT 0;

CREATE TABLE payment_order (
    id BIGINT NOT NULL AUTO_INCREMENT,
    amount_cents INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    order_no VARCHAR(40) NOT NULL,
    paid_at DATETIME(6) NULL,
    provider VARCHAR(20) NOT NULL,
    provider_trade_no VARCHAR(64) NULL,
    status VARCHAR(20) NOT NULL,
    updated_at DATETIME(6) NULL,
    report_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_order_order_no (order_no),
    KEY idx_payment_order_report_user (report_id, user_id),
    CONSTRAINT fk_payment_order_report FOREIGN KEY (report_id) REFERENCES report (id),
    CONSTRAINT fk_payment_order_user FOREIGN KEY (user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
