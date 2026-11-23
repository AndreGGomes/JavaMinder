# JavaMinder

Uma ferramenta para controlar gastos com assinaturas.

## Requisitos

- JDK 21 ou superior (`javac` e `java` no PATH) — o build usa `--release 21`

## Como compilar e executar

```bash
./build.sh     # compila para ./out
./run.sh       # abre a interface de terminal
```

Com dados de demonstração (cria a conta `demo@javaminnder.local` / `JavaMinder123`
já com quatro assinaturas, somente se não houver usuários):

```bash
./run.sh --seed-demo
```

Outras opções:

```bash
./run.sh --help
./run.sh --data /caminho/para/dados   # padrão: ./data
```

Também é possível rodar direto na JVM:

```bash
javac -encoding UTF-8 --release 21 -d out $(find src -name '*.java')
java -cp out javaminnder.Main
```

## O que o programa faz

- **Cadastro e login**: senha guardada como hash PBKDF2-HMAC-SHA256 (120k iterações,
  salt aleatório). O primeiro usuário cadastrado vira **ADMIN**.
- **Sessão**: `login`, `logout`, troca de senha.
- **Assinaturas**: listar, cadastrar, editar (nome, preço, ciclo, data, status,
  observações), remover, buscar por nome/observação e **registrar pagamento**
  (avança a próxima cobrança em um ciclo).
- **Status**: ativa, pausada ou cancelada — apenas as ativas entram nos totais.
- **Painel**: gasto mensal estimado, projeção anual, quantidade de assinaturas
  ativas e próxima cobrança.
- **Relatórios**: resumo por ciclo, cobranças dos próximos 30 dias, top 5 gastos.
- **Exportação CSV** das assinaturas em `data/exports/`.
- **Administração** (somente ADMIN): lista usuários e todas as assinaturas.

Valores usam `BigDecimal` e formatação em real; planos anuais são convertidos para
equivalente mensal (÷12) e planos mensais para anual (×12).

## Estrutura

```
JavaMinder/
├── build.sh / run.sh
├── src/javaminnder/
│   ├── Main.java              composição das dependências + laço principal
│   ├── CliOptions.java        argumentos de linha de comando
│   ├── DemoData.java          carga opcional de exemplo
│   ├── model/                 entidades e enums
│   ├── repository/            contrato de persistência + implementação JSON
│   ├── service/               regras de negócio (auth, assinaturas, dashboard, export)
│   ├── security/              hash de senha
│   ├── storage/               parser/serializador JSON escrito à mão
│   ├── util/                  validação, dinheiro e datas
│   └── ui/                    telas da CLI
└── data/                      gerado em tempo de execução (ignorado no git)
```

## Orientação a objetos

O projeto foi organizado para exercitar os conceitos pedidos:

| Conceito | Onde |
| --- | --- |
| **Interface** | `Describable` (implementada por `UserRole`, `BillingCycle`, `SubscriptionStatus` e pelas entidades) e `Repository<T>` (dois repositórios concretos) |
| **Herança** | `BaseEntity` → `User`, `Subscription`; `AbstractJsonRepository` → `UserRepository`, `SubscriptionRepository`; `AppException` → `ValidationException`, `AuthException`, `NotFoundException` |
| **Template Method** | `AbstractJsonRepository` cuida do arquivo e delega `toJson`/`fromJson` às subclasses |
| **Polimorfismo** | `Repository<T>` usado pela abstração; enums `BillingCycle`/`SubscriptionStatus`/`UserRole` implementando `Describable` |
| **Exceções próprias** | `AppException` → `ValidationException`, `AuthException`, `NotFoundException` |
| **Encapsulamento** | estado privado nas entidades; somente serviços alteram dados de negócio |

O **front-end não usa abstrações**: `ui/` tem apenas classes concretas (`AuthScreen`,
`MainMenuScreen`, `SubscriptionScreen`, `ReportScreen`, `AccountScreen`, `AdminScreen`)
e a classe utilitária `Menu` para formatar a saída. Toda interface, classe abstrata e
herança está no backend (`model`, `repository`, `service`, `security`), que é a parte
pensada para receber as regras de design by contract.

## Persistência em JSON

`data/users.json`:

```json
[
  {
    "id": 1,
    "username": "Demo",
    "email": "demo@javaminnder.local",
    "passwordHash": "pbkdf2$120000$...$...",
    "role": "ADMIN",
    "createdAt": "2026-11-18T09:30:00.000",
    "updatedAt": "2026-11-18T09:30:00.000"
  }
]
```

`data/subscriptions.json`:

```json
[
  {
    "id": 1,
    "userId": 1,
    "name": "Netflix",
    "price": "44.90",
    "cycle": "MONTHLY",
    "status": "ACTIVE",
    "nextBillingDate": "2026-11-25",
    "notes": "Plano padrão",
    "createdAt": "2026-11-18T09:30:00.000",
    "updatedAt": "2026-11-18T09:30:00.000"
  }
]
```

As gravações são atômicas (arquivo temporário + `ATOMIC_MOVE`), então uma
interrupção no meio da escrita não corrompe o JSON.

## Verificação JML (OpenJML)

As classes já anotadas (`service/Session` e os enums de `model/`) usam JML em
comentários `/*@ ... @*/`, ignorados pelo `javac`, e podem ser verificadas
estaticamente:

```bash
./jml-check.sh
```

O script compila com `./build.sh` e roda o `--esc` do OpenJML na lista `FILES`
(as demais classes entram pela classpath, sem specs).

Regra do projeto: **toda classe que for alterada é anotada por inteiro** (todos
os métodos e construtores com contrato), em ESC puro — sem `assume`, sem
`skipesc` e sem RAC. Quando uma biblioteca não tem contrato utilizável pelo ESC
(`BigDecimal`, `LocalDate`, `Optional`, `Collection.size`, …), a spec descreve só
o que é demonstrável (por exemplo `ensures \result != null`) ou o código é
trocado por uma versão provável.

A ferramenta não é dependência do projeto: baixe o release Linux do
[OpenJML 21](https://github.com/OpenJML/OpenJML/releases), extraia em
`.tools/openjml` (pasta ignorada no git) e rode o script.
