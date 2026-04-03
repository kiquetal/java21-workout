-- Panache uses GenerationType.SEQUENCE by default
-- These sequences must exist to match PanacheEntity's ID strategy

CREATE SEQUENCE book_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE member_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE book_lending_SEQ START WITH 1 INCREMENT BY 50;

-- Switch ID columns from BIGSERIAL (identity) to plain BIGINT (sequence-driven)
ALTER TABLE book ALTER COLUMN id DROP DEFAULT;
ALTER TABLE member ALTER COLUMN id DROP DEFAULT;
ALTER TABLE book_lending ALTER COLUMN id DROP DEFAULT;
