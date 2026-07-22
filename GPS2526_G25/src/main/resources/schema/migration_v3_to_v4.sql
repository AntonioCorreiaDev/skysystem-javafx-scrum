-- migration_v3_to_v4.sql
-- Adiciona suporte para histórico de notificações (Versão 4 da BD)

-- 1. Criar a tabela de notificações
CREATE TABLE IF NOT EXISTS trip_notifications (
                                                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                  trip_id INTEGER NOT NULL,
                                                  message TEXT NOT NULL,
                                                  timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                                                  is_read BOOLEAN DEFAULT 0,
                                                  FOREIGN KEY(trip_id) REFERENCES SAVED_TRIPS(trip_id) ON DELETE CASCADE
    );

-- 2. Criar índice para performance
CREATE INDEX IF NOT EXISTS idx_trip_notifications_trip_id ON trip_notifications(trip_id);