-- Cài extension nếu cần
CREATE EXTENSION IF NOT EXISTS dblink;

DO
$$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'auth_db') THEN
      PERFORM dblink_exec('CREATE DATABASE auth_db');
END IF;

   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'user_db') THEN
      PERFORM dblink_exec('CREATE DATABASE user_db');
END IF;
END
$$;

-- Tạo user nếu chưa có
DO
$$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'admin') THEN
CREATE ROLE admin WITH LOGIN PASSWORD 'admin123';
END IF;
END
$$;

-- Gán quyền
GRANT ALL PRIVILEGES ON DATABASE auth_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE user_db TO admin;
