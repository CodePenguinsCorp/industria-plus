# Roteiro do pitch final — Indústria Plus

Rascunho para a equipe revisar. Data prevista no cronograma: 04/12/2026. Confirmar duração e
formato com a professora. Os apresentadores e papéis N3 ainda devem ser definidos pela equipe.

## Abertura — problema e produto

“O Indústria Plus organiza a manutenção industrial: registra equipamentos, prioriza chamados e
distribui o atendimento entre técnicos. A regra crítica limita a dois os chamados de alta urgência
simultâneos por técnico.”

Mostrar rapidamente o fluxo da N1 e explicar a troca de equipes. Informar com precisão que o
pacote localizado foi compartilhado por Lucas no Teams; confirmar antes do pitch a autoria e a
identificação da entrega da equipe que o manteve.

## Evolução recebida

Apresentar login e perfis Administrador/Gestor/Técnico, troca obrigatória da senha provisória,
vínculo com gestor e histórico de serviços/peças. Diferenciar essas entregas das alterações da
retomada N3. Não atribuir ao grupo uma autoria ainda não confirmada.

## Demonstração da finalização

1. Entrar como administrador/gestor e abrir um equipamento.
2. Mostrar o histórico de chamados, intervenções e peças.
3. Informar um período e horas de operação medidas. Em dados de demonstração identificados como
   fictícios, 100 horas e duas falhas resultam em MTBF de 50 h.
4. Explicar que preventivas não contam como falhas e que o sistema diferencia ausência de dados
   de um período sem falhas.
5. Configurar preventiva com periodicidade e data vencida. Mostrar chamado criado, autor automático,
   histórico atualizado e próxima data avançada.
6. Repetir a verificação para mostrar a proteção contra duplicidade. Mostrar a opção de pausa.
7. Entrar como técnico para demonstrar a restrição de configurações e atendimento dos chamados
   atribuídos. Usar usuários de teste preparados antes da apresentação.

Se a professora exigir demonstração em tempo real, manter backend e banco ligados e preparar o
plano para vencer. Não depender de editar o banco durante o pitch.

## Qualidade e maturidade

- Expor as três regressões corrigidas: e-mail normalizado, bootstrap após migração e concorrência
  real no MySQL. Mostrar que H2 sozinho não encontrou a última.
- Apresentar 99 testes backend em H2/MySQL, teste dedicado de migração e 38 testes frontend.
  Não chamar contagem de testes de percentual de cobertura.
- Mostrar commits separados de recebimento, correções, evolução e documentação; explicar a
  revisão e o aceite que a equipe efetivamente realizou.
- Mostrar uma evidência do teste concorrente: uma aceitação, uma recusa e carga final igual a dois.

## Encerramento — limites e próximos passos

O produto usa horas de operação declaradas, não telemetria. Cada chamado corretivo representa uma
falha para esta versão. Há um plano e uma medição atuais por equipamento. Explicar a recuperação
de atrasos sem gerar todos os chamados retroativos.

Registrar o que ainda não foi homologado: produção com HTTPS real, recuperação de backup,
testes adicionais de falha de infraestrutura e aceite formal do PO/QA. Não declarar o sistema
“pronto para produção” apenas porque as suítes passaram.
