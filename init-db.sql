-- Tạo các database nếu chưa có
DO
$$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'auth_db') THEN
      RAISE NOTICE 'Creating database auth_db...';
   END IF;
END
$$;

-- Dùng lệnh CREATE DATABASE bên ngoài DO block
\connect postgres
CREATE DATABASE auth_db;
CREATE DATABASE user_db;

-- Tạo user nếu chưa có
DO
$$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'admin') THEN
      CREATE ROLE admin WITH LOGIN PASSWORD 'admin123';
   END IF;
END
$$;

-- Gán quyền cho user
GRANT ALL PRIVILEGES ON DATABASE auth_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE user_db TO admin;
