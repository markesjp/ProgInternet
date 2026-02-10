package autoescola;

import autoescola.dao.*;
import autoescola.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class TesteAutoescola {

    private static final Scanner scanner = new Scanner(System.in);
    
    // Instancia os DAOs para comunicação com o banco
    private static final InstrutorDao instrutorDao = new InstrutorDao();
    private static final AlunoDao alunoDao = new AlunoDao();
    private static final VeiculoDao veiculoDao = new VeiculoDao();
    private static final AulaDao aulaDao = new AulaDao();

    public static void main(String[] args) {
        int opcao = 0;
        do {
            exibirMenuPrincipal();
            try {
                opcao = Integer.parseInt(scanner.nextLine());
                switch (opcao) {
                    case 1: menuInstrutores(); break;
                    case 2: menuAlunos(); break;
                    case 3: menuVeiculos(); break;
                    case 4: menuAulas(); break;
                    case 0: System.out.println("Saindo do sistema..."); break;
                    default: System.out.println("❌ Opção inválida!");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Por favor, digite apenas números.");
            }
        } while (opcao != 0);
    }

    private static void exibirMenuPrincipal() {
        System.out.println("\n══════════════════════════════════════");
        System.out.println(" 🚗 SISTEMA DE AUTOESCOLA - PRINCIPAL");
        System.out.println("══════════════════════════════════════");
        System.out.println("1. Gerenciar Instrutores");
        System.out.println("2. Gerenciar Alunos");
        System.out.println("3. Gerenciar Veículos");
        System.out.println("4. Gerenciar Aulas (Agendamentos)");
        System.out.println("0. Sair");
        System.out.print("➤ Escolha uma opção: ");
    }

    // ========================================================================
    // MÓDULO: INSTRUTORES
    // ========================================================================
    private static void menuInstrutores() {
        System.out.println("\n--- 👨‍🏫 MENU INSTRUTORES ---");
        System.out.println("1. Cadastrar Novo");
        System.out.println("2. Listar Todos");
        System.out.println("3. Excluir por ID");
        System.out.print("Opção: ");
        
        try {
            int op = Integer.parseInt(scanner.nextLine());
            switch (op) {
                case 1:
                    System.out.print("Nome: ");
                    String nome = scanner.nextLine();
                    System.out.print("CPF: ");
                    String cpf = scanner.nextLine();
                    System.out.print("Telefone: ");
                    String tel = scanner.nextLine();
                    System.out.print("Especialidade (A, B, AB): ");
                    String esp = scanner.nextLine();
                    
                    Instrutor novo = new Instrutor(nome, cpf, tel, esp, LocalDate.now());
                    instrutorDao.inserir(novo);
                    break;
                case 2:
                    List<Instrutor> lista = instrutorDao.listar();
                    System.out.println("\n--- Lista de Instrutores ---");
                    for (Instrutor i : lista) System.out.println(i);
                    break;
                case 3:
                    System.out.print("ID para excluir: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    instrutorDao.remover(id);
                    break;
                default: System.out.println("Opção inválida.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // ========================================================================
    // MÓDULO: ALUNOS
    // ========================================================================
    private static void menuAlunos() {
        System.out.println("\n--- 🧑‍🎓 MENU ALUNOS ---");
        System.out.println("1. Cadastrar Novo");
        System.out.println("2. Listar Todos");
        System.out.println("3. Excluir por ID");
        System.out.print("Opção: ");
        
        try {
            int op = Integer.parseInt(scanner.nextLine());
            switch (op) {
                case 1:
                    System.out.print("Nome: ");
                    String nome = scanner.nextLine();
                    System.out.print("CPF: ");
                    String cpf = scanner.nextLine();
                    System.out.print("Telefone: ");
                    String tel = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Data Nascimento (AAAA-MM-DD): ");
                    LocalDate dataNasc = LocalDate.parse(scanner.nextLine());
                    System.out.print("Categoria Pretendida (A, B): ");
                    String cat = scanner.nextLine();

                    Aluno novo = new Aluno(nome, cpf, tel, email, dataNasc, cat, LocalDate.now());
                    alunoDao.inserir(novo);
                    break;
                case 2:
                    List<Aluno> lista = alunoDao.listar();
                    System.out.println("\n--- Lista de Alunos ---");
                    for (Aluno a : lista) System.out.println(a);
                    break;
                case 3:
                    System.out.print("ID para excluir: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    alunoDao.remover(id);
                    break;
            }
        } catch (Exception e) {
            System.out.println("Erro (verifique formato da data): " + e.getMessage());
        }
    }

    // ========================================================================
    // MÓDULO: VEÍCULOS
    // ========================================================================
    private static void menuVeiculos() {
        System.out.println("\n--- 🚗 MENU VEÍCULOS ---");
        System.out.println("1. Cadastrar Novo");
        System.out.println("2. Listar Todos");
        System.out.println("3. Listar Apenas DISPONÍVEIS");
        System.out.println("4. Excluir por ID");
        System.out.print("Opção: ");
        
        try {
            int op = Integer.parseInt(scanner.nextLine());
            switch (op) {
                case 1:
                    System.out.print("Placa: ");
                    String placa = scanner.nextLine();
                    System.out.print("Modelo: ");
                    String modelo = scanner.nextLine();
                    System.out.print("Marca: ");
                    String marca = scanner.nextLine();
                    System.out.print("Ano: ");
                    int ano = Integer.parseInt(scanner.nextLine());
                    System.out.print("Categoria (A/B): ");
                    String cat = scanner.nextLine();
                    
                    Veiculo novo = new Veiculo(placa, modelo, marca, ano, cat, "DISPONIVEL");
                    veiculoDao.inserir(novo);
                    break;
                case 2:
                    List<Veiculo> lista = veiculoDao.listar();
                    for (Veiculo v : lista) System.out.println(v);
                    break;
                case 3:
                    System.out.print("Qual categoria (A ou B)? ");
                    String c = scanner.nextLine();
                    List<Veiculo> disponiveis = veiculoDao.listarDisponiveis(c);
                    for (Veiculo v : disponiveis) System.out.println(v);
                    break;
                case 4:
                    System.out.print("ID para excluir: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    veiculoDao.remover(id);
                    break;
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // ========================================================================
    // MÓDULO: AULAS (O mais complexo)
    // ========================================================================
    private static void menuAulas() {
        System.out.println("\n--- 📅 MENU AULAS ---");
        System.out.println("1. Agendar Nova Aula");
        System.out.println("2. Listar Aulas (Detalhado)");
        System.out.println("3. Cancelar Aula (Remover)");
        System.out.print("Opção: ");
        
        try {
            int op = Integer.parseInt(scanner.nextLine());
            switch (op) {
                case 1:
                    agendarAula();
                    break;
                case 2:
                    // Usa o método JOIN que retorna String formatada
                    List<String> aulas = aulaDao.listarAulasCompletas();
                    if(aulas.isEmpty()) System.out.println("Nenhuma aula agendada.");
                    for (String s : aulas) System.out.println(s);
                    break;
                case 3:
                    System.out.print("ID da aula para cancelar: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    aulaDao.remover(id);
                    break;
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void agendarAula() {
        try {
            System.out.println("\n--- Novo Agendamento ---");
            
            System.out.print("ID do Aluno: ");
            int idAluno = Integer.parseInt(scanner.nextLine());
            if (alunoDao.buscarPorId(idAluno) == null) {
                System.out.println("❌ Aluno não encontrado!"); return;
            }

            System.out.print("ID do Instrutor: ");
            int idInstrutor = Integer.parseInt(scanner.nextLine());
            if (instrutorDao.buscarPorId(idInstrutor) == null) {
                System.out.println("❌ Instrutor não encontrado!"); return;
            }

            System.out.print("Tipo (PRATICA ou TEORICA): ");
            String tipo = scanner.nextLine().toUpperCase();
            
            Integer idVeiculo = null;
            if (tipo.equals("PRATICA")) {
                System.out.print("ID do Veículo: ");
                idVeiculo = Integer.parseInt(scanner.nextLine());
                Veiculo v = veiculoDao.buscarPorId(idVeiculo);
                if (v == null || !v.getStatus().equals("DISPONIVEL")) {
                    System.out.println("❌ Veículo inválido ou indisponível!"); return;
                }
            }

            System.out.print("Data e Hora (Ex: 2025-12-30T14:00): ");
            String dataStr = scanner.nextLine();
            LocalDateTime dataHora = LocalDateTime.parse(dataStr);

            System.out.print("Duração (minutos): ");
            int duracao = Integer.parseInt(scanner.nextLine());

            Aula novaAula = new Aula(idAluno, idInstrutor, idVeiculo, dataHora, duracao, tipo, "AGENDADA", "Sem obs");
            aulaDao.inserir(novaAula);
            
        } catch (DateTimeParseException dt) {
            System.out.println("❌ Formato de data inválido! Use AAAA-MM-DDTHH:MM (Ex: 2025-05-20T10:30)");
        } catch (NumberFormatException nf) {
            System.out.println("❌ Digite apenas números nos campos de ID e Duração.");
        } catch (RuntimeException re) {
            System.out.println("❌ Erro no banco de dados: " + re.getMessage());
        }
    }
}