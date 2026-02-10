-- =========================================================
-- AUTOESCOLA - MySQL 8.x (Workbench)
-- DDL + SEED (15+ registros por tabela)
-- =========================================================

-- 1) ZERAR E RECRIAR BANCO
DROP DATABASE IF EXISTS autoescola_db;
CREATE DATABASE autoescola_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE autoescola_db;

-- 2) TABELAS PRINCIPAIS
CREATE TABLE alunos (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  nome            VARCHAR(120) NOT NULL,
  cpf             VARCHAR(11)  NOT NULL,
  telefone        VARCHAR(20),
  email           VARCHAR(160),
  data_nascimento DATE,
  categoria_desejada VARCHAR(5),
  data_matricula  DATE NOT NULL DEFAULT (CURRENT_DATE),
  status          VARCHAR(10) NOT NULL DEFAULT 'ATIVO',
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT uq_alunos_cpf UNIQUE (cpf),
  CONSTRAINT chk_alunos_status CHECK (status IN ('ATIVO','INATIVO'))
) ENGINE=InnoDB;

CREATE INDEX idx_alunos_nome ON alunos(nome);
CREATE INDEX idx_alunos_status ON alunos(status);

CREATE TABLE instrutores (
  id               INT AUTO_INCREMENT PRIMARY KEY,
  nome             VARCHAR(120) NOT NULL,
  cpf              VARCHAR(11)  NOT NULL,
  telefone         VARCHAR(20),
  especialidade    VARCHAR(120),
  data_contratacao DATE NOT NULL DEFAULT (CURRENT_DATE),
  created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT uq_instrutores_cpf UNIQUE (cpf)
) ENGINE=InnoDB;

CREATE INDEX idx_instrutores_nome ON instrutores(nome);

CREATE TABLE veiculos (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  placa      VARCHAR(10) NOT NULL,
  modelo     VARCHAR(80) NOT NULL,
  marca      VARCHAR(60) NOT NULL,
  ano        INT NOT NULL,
  categoria  VARCHAR(5) NOT NULL,
  status     VARCHAR(15) NOT NULL DEFAULT 'DISPONIVEL',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT uq_veiculos_placa UNIQUE (placa),
  CONSTRAINT chk_veiculos_ano CHECK (ano BETWEEN 1990 AND 2100),
  CONSTRAINT chk_veiculos_status CHECK (status IN ('DISPONIVEL','MANUTENCAO','INDISPONIVEL'))
) ENGINE=InnoDB;

CREATE INDEX idx_veiculos_status ON veiculos(status);
CREATE INDEX idx_veiculos_categoria ON veiculos(categoria);

CREATE TABLE aulas (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  aluno_id        INT NOT NULL,
  instrutor_id    INT NOT NULL,
  veiculo_id      INT NULL,                 -- pode ser NULL para TEORICA
  data_aula       DATETIME NOT NULL,
  duracao_minutos INT NOT NULL,
  tipo            VARCHAR(10) NOT NULL,      -- TEORICA / PRATICA
  status          VARCHAR(12) NOT NULL DEFAULT 'MARCADA', -- MARCADA/DESMARCADA/CONCLUIDA
  observacoes     VARCHAR(255),
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT chk_aulas_duracao CHECK (duracao_minutos BETWEEN 30 AND 180),
  CONSTRAINT chk_aulas_tipo CHECK (tipo IN ('TEORICA','PRATICA')),
  CONSTRAINT chk_aulas_status CHECK (status IN ('MARCADA','DESMARCADA','CONCLUIDA')),

  CONSTRAINT fk_aulas_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,

  CONSTRAINT fk_aulas_instrutor FOREIGN KEY (instrutor_id) REFERENCES instrutores(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,

  CONSTRAINT fk_aulas_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_aulas_aluno_data ON aulas(aluno_id, data_aula);
CREATE INDEX idx_aulas_instrutor_data ON aulas(instrutor_id, data_aula);
CREATE INDEX idx_aulas_veiculo_data ON aulas(veiculo_id, data_aula);
CREATE INDEX idx_aulas_status ON aulas(status);

-- 3) HISTÓRICOS
CREATE TABLE alunos_historico (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  aluno_id    INT NOT NULL,
  changed_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  from_status VARCHAR(10),
  to_status   VARCHAR(10) NOT NULL,
  motivo      VARCHAR(255),
  actor       VARCHAR(80),

  CONSTRAINT chk_alunos_hist_to CHECK (to_status IN ('ATIVO','INATIVO')),
  CONSTRAINT chk_alunos_hist_from CHECK (from_status IS NULL OR from_status IN ('ATIVO','INATIVO')),

  CONSTRAINT fk_alunos_hist_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_alunos_hist_aluno_changed ON alunos_historico(aluno_id, changed_at);

CREATE TABLE aulas_historico (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  aula_id     INT NOT NULL,
  changed_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  from_status VARCHAR(12),
  to_status   VARCHAR(12) NOT NULL,
  motivo      VARCHAR(255),
  actor       VARCHAR(80),

  CONSTRAINT chk_aulas_hist_to CHECK (to_status IN ('MARCADA','DESMARCADA','CONCLUIDA')),
  CONSTRAINT chk_aulas_hist_from CHECK (from_status IS NULL OR from_status IN ('MARCADA','DESMARCADA','CONCLUIDA')),

  CONSTRAINT fk_aulas_hist_aula FOREIGN KEY (aula_id) REFERENCES aulas(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_aulas_hist_aula_changed ON aulas_historico(aula_id, changed_at);

-- =========================================================
-- 4) INSERTS (15+ por tabela)
-- =========================================================

INSERT INTO alunos (nome, cpf, telefone, email, data_nascimento, categoria_desejada, data_matricula, status) VALUES
('Ana Souza','18664926590','(49) 99100-2000','ana.souza@teste.com','1990-01-15','A','2024-01-10','ATIVO'),
('Bruno Pereira','93944595882','(49) 99101-2001','bruno.pereira@teste.com','1990-07-14','B','2024-01-30','ATIVO'),
('Carla Oliveira','19245277402','(49) 99102-2002','carla.oliveira@teste.com','1991-01-10','AB','2024-02-19','ATIVO'),
('Diego Santos','33386087647','(49) 99103-2003','diego.santos@teste.com','1991-07-09','ACC','2024-03-10','ATIVO'),
('Eduarda Lima','13666873456','(49) 99104-2004','eduarda.lima@teste.com','1992-01-05','B','2024-03-30','ATIVO'),
('Felipe Rocha','93176904754','(49) 99105-2005','felipe.rocha@teste.com','1992-07-03','A','2024-04-19','ATIVO'),
('Gabriela Martins','21260601870','(49) 99106-2006','gabriela.martins@teste.com','1992-12-30','B','2024-05-09','ATIVO'),
('Henrique Alves','15544616000','(49) 99107-2007','henrique.alves@teste.com','1993-06-28','AB','2024-05-29','ATIVO'),
('Isabela Costa','92447213921','(49) 99108-2008','isabela.costa@teste.com','1993-12-25','B','2024-06-18','ATIVO'),
('Joao Ribeiro','91398721220','(49) 99109-2009','joao.ribeiro@teste.com','1994-06-23','A','2024-07-08','ATIVO'),
('Karla Fernandes','27395250524','(49) 99110-2010','karla.fernandes@teste.com','1994-12-20','AB','2024-07-28','INATIVO'),
('Lucas Barbosa','07181455699','(49) 99111-2011','lucas.barbosa@teste.com','1995-06-18','B','2024-08-17','ATIVO'),
('Mariana Araujo','41481903284','(49) 99112-2012','mariana.araujo@teste.com','1995-12-15','ACC','2024-09-06','INATIVO'),
('Nicolas Teixeira','79108909075','(49) 99113-2013','nicolas.teixeira@teste.com','1996-06-12','B','2024-09-26','ATIVO'),
('Patricia Gomes','43784770762','(49) 99114-2014','patricia.gomes@teste.com','1996-12-09','A','2024-10-16','INATIVO');

INSERT INTO instrutores (nome, cpf, telefone, especialidade, data_contratacao) VALUES
('Andre Nogueira','27313107668','(49) 99300-4000','Direcao defensiva','2018-02-01'),
('Beatriz Melo','51466335920','(49) 99301-4001','Primeiros socorros','2018-04-02'),
('Carlos Freitas','50405346476','(49) 99302-4002','Baliza','2018-06-01'),
('Daniela Pires','07096192514','(49) 99303-4003','Direcao noturna','2018-07-31'),
('Elias Furtado','04736746700','(49) 99304-4004','Direcao em rodovia','2018-09-29'),
('Fernanda Dias','68385014225','(49) 99305-4005','Direcao urbana','2018-11-28'),
('Gustavo Lopes','30935034773','(49) 99306-4006','Mecanica basica','2019-01-27'),
('Helena Cardoso','07551243160','(49) 99307-4007','Direcao economica','2019-03-28'),
('Igor Batista','29968020800','(49) 99308-4008','Direcao em chuva','2019-05-27'),
('Juliana Monteiro','13887180739','(49) 99309-4009','Psicotecnico','2019-07-26'),
('Kevin Moraes','61656392836','(49) 99310-4010','Legislacao','2019-09-24'),
('Larissa Campos','39147619201','(49) 99311-4011','Direcao avancada','2019-11-23'),
('Mateus Rezende','50541063081','(49) 99312-4012','Direcao para habilitados','2020-01-22'),
('Natalia Ramos','79556901337','(49) 99313-4013','Reciclagem','2020-03-22'),
('Otavio Vieira','48957010777','(49) 99314-4014','Aulas teoricas','2020-05-21');

INSERT INTO veiculos (placa, modelo, marca, ano, categoria, status) VALUES
('ABC-1234','Gol','VW',2016,'B','DISPONIVEL'),
('DEF-5678','Onix','Chevrolet',2019,'B','DISPONIVEL'),
('GHI-9012','HB20','Hyundai',2020,'B','MANUTENCAO'),
('JKL-3456','Civic','Honda',2015,'B','DISPONIVEL'),
('MNO-7890','Corolla','Toyota',2018,'B','DISPONIVEL'),
('PQR-1122','Ka','Ford',2017,'B','INDISPONIVEL'),
('STU-3344','Uno','Fiat',2014,'B','DISPONIVEL'),
('VWX-5566','Argo','Fiat',2021,'B','DISPONIVEL'),
('YZA-7788','Polo','VW',2022,'B','MANUTENCAO'),
('BCD-9900','Sandero','Renault',2016,'B','DISPONIVEL'),
('EFG-1357','Fiesta','Ford',2013,'B','DISPONIVEL'),
('HIJ-2468','City','Honda',2019,'B','DISPONIVEL'),
('KLM-3690','Cruze','Chevrolet',2017,'B','INDISPONIVEL'),
('NOP-4812','Compass','Jeep',2020,'B','DISPONIVEL'),
('QRS-5934','Renegade','Jeep',2021,'B','DISPONIVEL');

-- 20 aulas (mistura de PRATICA/TEORICA, futuros/passados, status variados)
INSERT INTO aulas (aluno_id, instrutor_id, veiculo_id, data_aula, duracao_minutos, tipo, status, observacoes) VALUES
(1,  1,  1,  DATE_ADD(NOW(), INTERVAL -20 DAY) + INTERVAL 9 HOUR,  50, 'PRATICA', 'CONCLUIDA', 'Concluida (baliza).'),
(1,  2,  2,  DATE_ADD(NOW(), INTERVAL 2 DAY)  + INTERVAL 10 HOUR, 50, 'PRATICA', 'MARCADA',  'Aula pratica futura.'),
(1,  3, NULL, DATE_ADD(NOW(), INTERVAL 1 DAY)  + INTERVAL 19 HOUR, 60, 'TEORICA', 'MARCADA',  'Teorica: legislacao.'),
(2,  4,  4,  DATE_ADD(NOW(), INTERVAL 3 DAY)  + INTERVAL 8 HOUR,  50, 'PRATICA', 'MARCADA',  'Estacionamento.'),
(2,  5,  5,  DATE_ADD(NOW(), INTERVAL -10 DAY)+ INTERVAL 14 HOUR, 50, 'PRATICA', 'CONCLUIDA','Sem ocorrencias.'),
(3,  6,  7,  DATE_ADD(NOW(), INTERVAL 5 DAY)  + INTERVAL 9 HOUR,  50, 'PRATICA', 'MARCADA',  'Direcao urbana.'),
(3,  7, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY)+ INTERVAL 18 HOUR, 60, 'TEORICA', 'CONCLUIDA','Teorica concluida.'),
(4,  8,  8,  DATE_ADD(NOW(), INTERVAL 7 DAY)  + INTERVAL 11 HOUR, 50, 'PRATICA', 'MARCADA',  'Rodovia.'),
(4,  9,  9,  DATE_ADD(NOW(), INTERVAL 8 DAY)  + INTERVAL 15 HOUR, 50, 'PRATICA', 'MARCADA',  'Chuva simulada.'),
(5, 10, NULL, DATE_ADD(NOW(), INTERVAL 4 DAY)  + INTERVAL 20 HOUR, 60, 'TEORICA', 'MARCADA',  'Primeiros socorros.'),
(5, 11, 10, DATE_ADD(NOW(), INTERVAL -2 DAY) + INTERVAL 9 HOUR,  50, 'PRATICA', 'DESMARCADA','Desmarcada pelo aluno.'),
(6, 12, 11, DATE_ADD(NOW(), INTERVAL 9 DAY)  + INTERVAL 10 HOUR, 50, 'PRATICA', 'MARCADA',  'Treino de rampa.'),
(7, 13, 12, DATE_ADD(NOW(), INTERVAL 10 DAY)+ INTERVAL 8 HOUR,  50, 'PRATICA', 'MARCADA',  'Avancada.'),
(8, 14, NULL,DATE_ADD(NOW(), INTERVAL 6 DAY)  + INTERVAL 19 HOUR, 60, 'TEORICA', 'MARCADA',  'Reciclagem.'),
(9, 15, 14, DATE_ADD(NOW(), INTERVAL -1 DAY) + INTERVAL 16 HOUR, 50, 'PRATICA', 'CONCLUIDA','Avaliacao positiva.'),
(10, 1, 15, DATE_ADD(NOW(), INTERVAL 12 DAY)+ INTERVAL 9 HOUR,  50, 'PRATICA', 'MARCADA',  'Simulado prova.'),
(11, 2,  1, DATE_ADD(NOW(), INTERVAL 2 DAY) + INTERVAL 14 HOUR, 50, 'PRATICA', 'DESMARCADA','Aluno inativo (teste).'),
(13, 3,  2, DATE_ADD(NOW(), INTERVAL 3 DAY) + INTERVAL 17 HOUR, 50, 'PRATICA', 'DESMARCADA','Aluno inativo (teste).'),
(15, 4, NULL,DATE_ADD(NOW(), INTERVAL 1 DAY) + INTERVAL 9 HOUR,  60, 'TEORICA', 'DESMARCADA','Aluno inativo (teste).'),
(12, 5,  4, DATE_ADD(NOW(), INTERVAL 15 DAY)+ INTERVAL 11 HOUR, 50, 'PRATICA', 'MARCADA',  'Direcao noturna.');

-- Historico alunos (15)
INSERT INTO alunos_historico (aluno_id, changed_at, from_status, to_status, motivo, actor)
SELECT id, NOW(), NULL, status, 'Carga inicial', 'seed' FROM alunos;

-- Historico aulas (20)
INSERT INTO aulas_historico (aula_id, changed_at, from_status, to_status, motivo, actor)
SELECT id, data_aula, NULL, status, 'Carga inicial aula', 'seed' FROM aulas;

-- =========================================================
-- FIM
-- =========================================================
