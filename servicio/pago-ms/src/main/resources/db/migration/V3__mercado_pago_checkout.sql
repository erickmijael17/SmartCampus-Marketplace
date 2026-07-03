ALTER TABLE pagos
    ADD COLUMN IF NOT EXISTS mp_preference_id VARCHAR(120),
    ADD COLUMN IF NOT EXISTS mp_payment_id VARCHAR(80),
    ADD COLUMN IF NOT EXISTS mp_status VARCHAR(50),
    ADD COLUMN IF NOT EXISTS checkout_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS external_reference VARCHAR(120),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_pagos_external_reference ON pagos (external_reference);
CREATE INDEX IF NOT EXISTS idx_pagos_mp_payment_id ON pagos (mp_payment_id);
