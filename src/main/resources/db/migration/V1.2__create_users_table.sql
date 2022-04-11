CREATE TABLE IF NOT EXISTS auth.users(
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role APPLICATION_ROLE NOT NULL,
  date_added TIMESTAMP NOT NULL 
)