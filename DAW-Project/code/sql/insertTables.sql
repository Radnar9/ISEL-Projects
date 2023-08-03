BEGIN;
    INSERT INTO "USER"(id, name, email, password) VALUES
        ('cf128ed3-0d65-42d9-8c96-8ff2e05b3d08', 'José Bonifácio', 'joca@gmail.com', 'soubuefixe'),
        ('9f5246de-3a02-4f91-9768-ec576311a523', 'Pedro Moreira', 'pedrocas@outlook.com', 'soubuerico'),
        ('b54f4f46-5833-4aae-a205-456da878ebc2', 'Zé Pedro', 'zezinho@hotmail.com', 'souboapessoa'),
        ('27f6016a-a891-459e-85d6-7df20815769a', 'Guilherme Ronaldo', 'guizado@hotmail.com', 'souoronaldo');

    INSERT INTO PROJECT(name, description, "user") VALUES
        ('Caparica Metropolitano', 'Expansão do metro para a zona da Costa da Caparica', 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08'),
        ('CCISEL Demolição', 'Demolição do Centro de Cálculo do ISEL', '9f5246de-3a02-4f91-9768-ec576311a523'),
        ('Passadiço do Tejo', 'Expansão do passadiço, ligação de Santa Iria ao Parque das Nações', 'b54f4f46-5833-4aae-a205-456da878ebc2');

    INSERT INTO LABEL(name, project) VALUES
        ('defect', 1),
        ('new-functionality', 1),
        ('exploration', 1),
        ('defect', 2),
        ('new-functionality', 2),
        ('exploration', 2),
        ('defect', 3),
        ('new-functionality', 3),
        ('exploration', 3);

    INSERT INTO STATE(name, initial, project) VALUES
        ('closed', false, 1),   -- 1
        ('archived', false, 1), -- 2
        ('todo', true, 1),      -- 3
        ('wip', false, 1),      -- 4
        ('closed', false, 2),
        ('archived', false, 2),
        ('todo', true, 2),
        ('wip', false, 2),
        ('closed', false, 3),
        ('archived', false, 3),
        ('todo', true, 3),
        ('wip', false, 3);

    INSERT INTO STATE_TRANSITION(current_state, next_state, project) VALUES
        (1, 2, 1), -- closed -> archived
        (4, 1, 1), -- wip -> closed
        (3, 4, 1), -- todo_ -> wip
        (5, 6, 2), -- closed -> archived
        (8, 5, 2), -- wip -> closed
        (7, 8, 2), -- todo_ -> wip
        (9, 10, 3), -- closed -> archived
        (12, 9, 3), -- wip -> closed
        (11, 12, 3); -- todo_ -> wip

    INSERT INTO ISSUE(name, description, state, project) VALUES
        ('Construir perímetro de segurança', 'Projetar e implementar um perímetro de segurança para a inicialização das obras', 3, 1),
        ('Retirar alcatrão', 'Remoção do alcatrão da estrada, onde vai passar o futuro metro', 2, 1),
        ('Desmontar interior', 'Retirar o necessário do interior do CC', 7, 2),
        ('Limpar a margem do rio Tejo', 'Retirar o lixo e o lodo onde serão construídas os suportes do passadiço', 11, 3);

    INSERT INTO ISSUE_LABEL(issue, project, label) VALUES
        (1, 1, 2), -- Construir perímetro de segurança - new-functionality
        (2, 1, 1), -- Retirar alcatrão - defect
        (3, 2, 3); -- Desmontar interior - exploration

    INSERT INTO COMMENT(comment, timestamp, issue, project) VALUES
        ('Investimento muito elevado, deviam pensar melhor', '2022-04-08 21:52:47.012620', 1, 1),
        ('Façam apenas durante a manhã, durante a tarde tem muita circulação', '2022-04-08 21:52:47.012620', 2, 1),
        ('Cuidado com os peixinhos, eles não têm que sair prejudicados', '2022-04-08 21:52:47.012620', 4, 3);
COMMIT;