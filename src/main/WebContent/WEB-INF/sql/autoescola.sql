-- =========================================================
-- AUTOESCOLA (PostgreSQL) - DDL + SEED COMPLETO
-- Rodar no pgAdmin (PostgreSQL 12+)
-- =========================================================

BEGIN;

-- (Opcional) usar um schema específico
DROP SCHEMA IF EXISTS autoescola CASCADE;
CREATE SCHEMA autoescola;
SET search_path TO autoescola;

-- =============================
-- Tipos ENUM (status)
-- =============================
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'aluno_status') THEN
    CREATE TYPE aluno_status AS ENUM ('ATIVO', 'INATIVO');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'veiculo_status') THEN
    CREATE TYPE veiculo_status AS ENUM ('DISPONIVEL', 'MANUTENCAO', 'INDISPONIVEL');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'aula_tipo') THEN
    CREATE TYPE aula_tipo AS ENUM ('TEORICA', 'PRATICA');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'aula_status') THEN
    CREATE TYPE aula_status AS ENUM ('MARCADA', 'DESMARCADA', 'CONCLUIDA');
  END IF;
END $$;

-- =============================
-- Tabelas principais
-- =============================

DROP TABLE IF EXISTS aulas_historico CASCADE;
DROP TABLE IF EXISTS alunos_historico CASCADE;
DROP TABLE IF EXISTS aulas CASCADE;
DROP TABLE IF EXISTS veiculos CASCADE;
DROP TABLE IF EXISTS instrutores CASCADE;
DROP TABLE IF EXISTS alunos CASCADE;

CREATE TABLE alunos (
  id            SERIAL PRIMARY KEY,
  nome          VARCHAR(120) NOT NULL,
  cpf           VARCHAR(11)  NOT NULL UNIQUE,
  telefone      VARCHAR(20),
  email         VARCHAR(160),
  data_nascimento DATE,
  categoria_desejada VARCHAR(5),
  data_matricula DATE NOT NULL DEFAULT CURRENT_DATE,
  status        aluno_status NOT NULL DEFAULT 'ATIVO',
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_alunos_status ON alunos(status);
CREATE INDEX idx_alunos_nome ON alunos(nome);

CREATE TABLE instrutores (
  id            SERIAL PRIMARY KEY,
  nome          VARCHAR(120) NOT NULL,
  cpf           VARCHAR(11) NOT NULL UNIQUE,
  telefone      VARCHAR(20),
  especialidade VARCHAR(120),
  data_contratacao DATE NOT NULL DEFAULT CURRENT_DATE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_instrutores_nome ON instrutores(nome);

CREATE TABLE veiculos (
  id            SERIAL PRIMARY KEY,
  placa         VARCHAR(10) NOT NULL UNIQUE,
  modelo        VARCHAR(80) NOT NULL,
  marca         VARCHAR(60) NOT NULL,
  ano           INT NOT NULL CHECK (ano >= 1990 AND ano <= 2100),
  categoria     VARCHAR(5) NOT NULL,
  status        veiculo_status NOT NULL DEFAULT 'DISPONIVEL',
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_veiculos_status ON veiculos(status);
CREATE INDEX idx_veiculos_categoria ON veiculos(categoria);

CREATE TABLE aulas (
  id            SERIAL PRIMARY KEY,
  aluno_id      INT NOT NULL REFERENCES alunos(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  instrutor_id  INT NOT NULL REFERENCES instrutores(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  veiculo_id    INT REFERENCES veiculos(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  data_aula     TIMESTAMPTZ NOT NULL,
  duracao_minutos INT NOT NULL CHECK (duracao_minutos BETWEEN 30 AND 180),
  tipo          aula_tipo NOT NULL,
  status        aula_status NOT NULL DEFAULT 'MARCADA',
  observacoes   VARCHAR(255),
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_aulas_aluno_data ON aulas(aluno_id, data_aula);
CREATE INDEX idx_aulas_instrutor_data ON aulas(instrutor_id, data_aula);
CREATE INDEX idx_aulas_veiculo_data ON aulas(veiculo_id, data_aula);
CREATE INDEX idx_aulas_status ON aulas(status);

-- =============================
-- Histórico
-- =============================

CREATE TABLE alunos_historico (
  id          SERIAL PRIMARY KEY,
  aluno_id    INT NOT NULL REFERENCES alunos(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  changed_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  from_status aluno_status,
  to_status   aluno_status NOT NULL,
  motivo      VARCHAR(255),
  actor       VARCHAR(80)
);

CREATE INDEX idx_alunos_hist_aluno_changed ON alunos_historico(aluno_id, changed_at);

CREATE TABLE aulas_historico (
  id          SERIAL PRIMARY KEY,
  aula_id     INT NOT NULL REFERENCES aulas(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  changed_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  from_status aula_status,
  to_status   aula_status NOT NULL,
  motivo      VARCHAR(255),
  actor       VARCHAR(80)
);

CREATE INDEX idx_aulas_hist_aula_changed ON aulas_historico(aula_id, changed_at);

-- =============================
-- Triggers para updated_at
-- =============================

CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at := NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_alunos_updated ON alunos;
CREATE TRIGGER trg_alunos_updated
BEFORE UPDATE ON alunos
FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

DROP TRIGGER IF EXISTS trg_instrutores_updated ON instrutores;
CREATE TRIGGER trg_instrutores_updated
BEFORE UPDATE ON instrutores
FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

DROP TRIGGER IF EXISTS trg_veiculos_updated ON veiculos;
CREATE TRIGGER trg_veiculos_updated
BEFORE UPDATE ON veiculos
FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

DROP TRIGGER IF EXISTS trg_aulas_updated ON aulas;
CREATE TRIGGER trg_aulas_updated
BEFORE UPDATE ON aulas
FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- =============================
-- SEED: 15 alunos
-- =============================
INSERT INTO alunos (id, nome, cpf, telefone, email, data_nascimento, categoria_desejada, data_matricula, status)
VALUES
(1,'Ana Souza','18664926590','(49) 99100-2000','ana.souza@teste.com','1990-01-15','A','2024-01-10','ATIVO'),
(2,'Bruno Pereira','93944595882','(49) 99101-2001','bruno.pereira@teste.com','1990-07-14','B','2024-01-30','ATIVO'),
(3,'Carla Oliveira','19245277402','(49) 99102-2002','carla.oliveira@teste.com','1991-01-10','AB','2024-02-19','ATIVO'),
(4,'Diego Santos','33386087647','(49) 99103-2003','diego.santos@teste.com','1991-07-09','ACC','2024-03-10','ATIVO'),
(5,'Eduarda Lima','13666873456','(49) 99104-2004','eduarda.lima@teste.com','1992-01-05','B','2024-03-30','ATIVO'),
(6,'Felipe Rocha','93176904754','(49) 99105-2005','felipe.rocha@teste.com','1992-07-03','A','2024-04-19','ATIVO'),
(7,'Gabriela Martins','21260601870','(49) 99106-2006','gabriela.martins@teste.com','1992-12-30','B','2024-05-09','ATIVO'),
(8,'Henrique Alves','15544616000','(49) 99107-2007','henrique.alves@teste.com','1993-06-28','AB','2024-05-29','ATIVO'),
(9,'Isabela Costa','92447213921','(49) 99108-2008','isabela.costa@teste.com','1993-12-25','B','2024-06-18','ATIVO'),
(10,'Joao Ribeiro','91398721220','(49) 99109-2009','joao.ribeiro@teste.com','1994-06-23','A','2024-07-08','ATIVO'),
(11,'Karla Fernandes','27395250524','(49) 99110-2010','karla.fernandes@teste.com','1994-12-20','AB','2024-07-28','INATIVO'),
(12,'Lucas Barbosa','07181455699','(49) 99111-2011','lucas.barbosa@teste.com','1995-06-18','B','2024-08-17','ATIVO'),
(13,'Mariana Araujo','41481903284','(49) 99112-2012','mariana.araujo@teste.com','1995-12-15','ACC','2024-09-06','INATIVO'),
(14,'Nicolas Teixeira','79108909075','(49) 99113-2013','nicolas.teixeira@teste.com','1996-06-12','B','2024-09-26','ATIVO'),
(15,'Patricia Gomes','43784770762','(49) 99114-2014','patricia.gomes@teste.com','1996-12-09','A','2024-10-16','INATIVO');

-- =============================
-- SEED: 15 instrutores
-- =============================
INSERT INTO instrutores (id, nome, cpf, telefone, especialidade, data_contratacao)
VALUES
(1,'Andre Nogueira','27313107668','(49) 99300-4000','Direção defensiva','2018-02-01'),
(2,'Beatriz Melo','51466335920','(49) 99301-4001','Primeiros socorros','2018-04-02'),
(3,'Carlos Freitas','50405346476','(49) 99302-4002','Baliza','2018-06-01'),
(4,'Daniela Pires','07096192514','(49) 99303-4003','Direção noturna','2018-07-31'),
(5,'Elias Furtado','04736746700','(49) 99304-4004','Direção em rodovia','2018-09-29'),
(6,'Fernanda Dias','68385014225','(49) 99305-4005','Direção urbana','2018-11-28'),
(7,'Gustavo Lopes','30935034773','(49) 99306-4006','Mecânica básica','2019-01-27'),
(8,'Helena Cardoso','07551243160','(49) 99307-4007','Direção econômica','2019-03-28'),
(9,'Igor Batista','29968020800','(49) 99308-4008','Direção em chuva','2019-05-27'),
(10,'Juliana Monteiro','13887180739','(49) 99309-4009','Psicotécnico','2019-07-26'),
(11,'Kevin Moraes','61656392836','(49) 99310-4010','Legislação','2019-09-24'),
(12,'Larissa Campos','39147619201','(49) 99311-4011','Direção avançada','2019-11-23'),
(13,'Mateus Rezende','50541063081','(49) 99312-4012','Direção para habilitados','2020-01-22'),
(14,'Natalia Ramos','79556901337','(49) 99313-4013','Reciclagem','2020-03-22'),
(15,'Otavio Vieira','48957010777','(49) 99314-4014','Aulas teóricas','2020-05-21');

-- =============================
-- SEED: 15 veiculos
-- =============================
INSERT INTO veiculos (id, placa, modelo, marca, ano, categoria, status)
VALUES
(1,'ABC-1234','Gol','VW',2016,'B','DISPONIVEL'),
(2,'DEF-5678','Onix','Chevrolet',2019,'B','DISPONIVEL'),
(3,'GHI-9012','HB20','Hyundai',2020,'B','MANUTENCAO'),
(4,'JKL-3456','Civic','Honda',2015,'B','DISPONIVEL'),
(5,'MNO-7890','Corolla','Toyota',2018,'B','DISPONIVEL'),
(6,'PQR-1122','Ka','Ford',2017,'B','INDISPONIVEL'),
(7,'STU-3344','Uno','Fiat',2014,'B','DISPONIVEL'),
(8,'VWX-5566','Argo','Fiat',2021,'B','DISPONIVEL'),
(9,'YZA-7788','Polo','VW',2022,'B','MANUTENCAO'),
(10,'BCD-9900','Sandero','Renault',2016,'B','DISPONIVEL'),
(11,'EFG-1357','Fiesta','Ford',2013,'B','DISPONIVEL'),
(12,'HIJ-2468','City','Honda',2019,'B','DISPONIVEL'),
(13,'KLM-3690','Cruze','Chevrolet',2017,'B','INDISPONIVEL'),
(14,'NOP-4812','Compass','Jeep',2020,'B','DISPONIVEL'),
(15,'QRS-5934','Renegade','Jeep',2021,'B','DISPONIVEL');

-- =============================
-- SEED: 20 aulas (passadas e futuras)
-- Regras para teste:
-- - Algumas CONCLUIDAS no passado
-- - Algumas MARCADAS no futuro
-- - Algumas DESMARCADAS (inclusive futuras em alunos inativos)
-- - Teóricas podem ter veiculo_id NULL
-- =============================
INSERT INTO aulas (id, aluno_id, instrutor_id, veiculo_id, data_aula, duracao_minutos, tipo, status, observacoes)
VALUES
(1, 1, 1, 1,  (NOW() - INTERVAL '20 days') + INTERVAL '9 hours',  50, 'PRATICA', 'CONCLUIDA', 'Concluída (baliza).'),
(2, 1, 2, 2,  (NOW() + INTERVAL '2 days')  + INTERVAL '10 hours', 50, 'PRATICA', 'MARCADA',  'Aula prática futura.'),
(3, 1, 3, NULL,(NOW() + INTERVAL '1 day')  + INTERVAL '19 hours', 60, 'TEORICA', 'MARCADA',  'Teórica: legislação.'),
(4, 2, 4, 4,  (NOW() + INTERVAL '3 days')  + INTERVAL '8 hours',  50, 'PRATICA', 'MARCADA',  'Estacionamento.'),
(5, 2, 5, 5,  (NOW() - INTERVAL '10 days') + INTERVAL '14 hours', 50, 'PRATICA', 'CONCLUIDA','Sem ocorrências.'),
(6, 3, 6, 7,  (NOW() + INTERVAL '5 days')  + INTERVAL '9 hours',  50, 'PRATICA', 'MARCADA',  'Direção urbana.'),
(7, 3, 7, NULL,(NOW() - INTERVAL '5 days') + INTERVAL '18 hours', 60, 'TEORICA', 'CONCLUIDA','Teórica concluída.'),
(8, 4, 8, 8,  (NOW() + INTERVAL '7 days')  + INTERVAL '11 hours', 50, 'PRATICA', 'MARCADA',  'Rodovia.'),
(9, 4, 9, 9,  (NOW() + INTERVAL '8 days')  + INTERVAL '15 hours', 50, 'PRATICA', 'MARCADA',  'Chuva simulada.'),
(10,5,10,NULL,(NOW() + INTERVAL '4 days')  + INTERVAL '20 hours', 60, 'TEORICA', 'MARCADA',  'Primeiros socorros.'),
(11,5,11,10,(NOW() - INTERVAL '2 days')  + INTERVAL '9 hours',  50, 'PRATICA', 'DESMARCADA','Desmarcada pelo aluno.'),
(12,6,12,11,(NOW() + INTERVAL '9 days')  + INTERVAL '10 hours', 50, 'PRATICA', 'MARCADA',  'Treino de rampa.'),
(13,7,13,12,(NOW() + INTERVAL '10 days') + INTERVAL '8 hours',  50, 'PRATICA', 'MARCADA',  'Avançada.'),
(14,8,14,NULL,(NOW() + INTERVAL '6 days')  + INTERVAL '19 hours', 60, 'TEORICA', 'MARCADA',  'Reciclagem.'),
(15,9,15,14,(NOW() - INTERVAL '1 day')  + INTERVAL '16 hours', 50, 'PRATICA', 'CONCLUIDA','Avaliação positiva.'),
(16,10,1,15,(NOW() + INTERVAL '12 days') + INTERVAL '9 hours',  50, 'PRATICA', 'MARCADA',  'Simulado prova.'),
(17,11,2,1,(NOW() + INTERVAL '2 days')  + INTERVAL '14 hours', 50, 'PRATICA', 'DESMARCADA','Aluno INATIVO (seed).'),
(18,13,3,2,(NOW() + INTERVAL '3 days')  + INTERVAL '17 hours', 50, 'PRATICA', 'DESMARCADA','Aluno INATIVO (seed).'),
(19,15,4,NULL,(NOW() + INTERVAL '1 day')  + INTERVAL '9 hours',  60, 'TEORICA', 'DESMARCADA','Aluno INATIVO (seed).'),
(20,12,5,4,(NOW() + INTERVAL '15 days') + INTERVAL '11 hours', 50, 'PRATICA', 'MARCADA',  'Direção noturna.');

-- =============================
-- SEED: Histórico de alunos (15)
-- =============================
INSERT INTO alunos_historico (id, aluno_id, changed_at, from_status, to_status, motivo, actor)
SELECT
  a.id,
  a.id AS aluno_id,
  NOW() - (a.id || ' days')::interval AS changed_at,
  NULL::aluno_status AS from_status,
  a.status AS to_status,
  'Carga inicial' AS motivo,
  'seed' AS actor
FROM alunos a
ORDER BY a.id;

-- =============================
-- SEED: Histórico de aulas (20)
-- =============================
INSERT INTO aulas_historico (id, aula_id, changed_at, from_status, to_status, motivo, actor)
SELECT
  au.id,
  au.id AS aula_id,
  au.data_aula AS changed_at,
  NULL::aula_status AS from_status,
  au.status AS to_status,
  'Carga inicial aula' AS motivo,
  'seed' AS actor
FROM aulas au
ORDER BY au.id;

-- Ajustar sequences para continuar do maior id
SELECT setval(pg_get_serial_sequence('alunos','id'), (SELECT MAX(id) FROM alunos));
SELECT setval(pg_get_serial_sequence('instrutores','id'), (SELECT MAX(id) FROM instrutores));
SELECT setval(pg_get_serial_sequence('veiculos','id'), (SELECT MAX(id) FROM veiculos));
SELECT setval(pg_get_serial_sequence('aulas','id'), (SELECT MAX(id) FROM aulas));
SELECT setval(pg_get_serial_sequence('alunos_historico','id'), (SELECT MAX(id) FROM alunos_historico));
SELECT setval(pg_get_serial_sequence('aulas_historico','id'), (SELECT MAX(id) FROM aulas_historico));

COMMIT;
