CREATE TABLE IF NOT EXISTS history.history(
  id BIGSERIAL PRIMARY KEY,
  history_type VARCHAR(255) NOT NULL,
  user_id BIGINT REFERENCES auth.users(id),
  result VARCHAR(255) NOT NULL,
  execution_time BIGINT NOT NULL,
  date_added TIMESTAMP NOT NULL 
)