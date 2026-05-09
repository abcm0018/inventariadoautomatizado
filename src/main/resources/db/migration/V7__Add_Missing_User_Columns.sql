-- Columna añadida a la entidad User.java sin migration correspondiente.
ALTER TABLE users ADD COLUMN inactive BIT NOT NULL DEFAULT 0;
