CREATE TABLE IF NOT EXISTS registry.cars(
  id BIGSERIAL PRIMARY KEY,
  number VARCHAR(255) UNIQUE NOT NULL,
  manufacture VARCHAR(255) NOT NULL,
  year INT NOT NULL,
  user_id BIGINT NOT NULL REFERENCES auth.users(id),
  color VARCHAR(255) NOT NULL,
  date_added TIMESTAMP NOT NULL 
)