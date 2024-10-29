# IscTorrent

IscTorrent é um sistema distribuído de partilha de ficheiros desenvolvido como projeto para a disciplina de Programação Concorrente e Distribuída (PCD) no ano letivo de 2024/25. Este sistema permite que vários utilizadores numa rede P2P (Peer-to-Peer) compartilhem e descarreguem ficheiros binários (ex.: ficheiros de áudio) diretamente entre si, sem a necessidade de um servidor central.

## Objetivos

Este projeto visa desenvolver competências em programação concorrente e distribuída, aplicando conceitos essenciais de sincronização e gestão de threads, com foco na troca de mensagens entre nós de uma rede P2P.

## Funcionalidades

- **Arquitetura P2P:** Cada nó comunica diretamente com os nós que conhece, sem recorrer a um servidor central.
- **Conexão entre nós:** Os utilizadores podem conectar-se a outros nós inserindo o endereço de cada nó através da GUI.
- **Pesquisa e descarregamento de ficheiros:**
  - Realizar buscas por palavras-chave e listar ficheiros disponíveis nos nós conectados.
  - Pedir o descarregamento de um ficheiro disponível em outros nós.
- **Descarregamento por blocos:** O descarregamento é feito em blocos de tamanho padrão (ex.: 10 KB), e os blocos podem ser obtidos simultaneamente de vários nós.
- **Multi-threading e sincronização:** Cada nó pode iniciar múltiplas threads para download de ficheiros e responder a pedidos de outros nós.

## Interface Gráfica (GUI)

A GUI permite ao utilizador:
- Estabelecer conexão com outros nós.
- Pesquisar ficheiros por palavras-chave.
- Visualizar resultados da pesquisa com o número de nós que disponibilizam cada ficheiro.
- Solicitar o download de ficheiros disponíveis em outros nós.

## Estrutura do Projeto

- **Conexão com Nós:** Implementada via `NewConnectionRequest`, permitindo a conexão ativa ou passiva com outros nós.
- **Mensagens de Comunicação:** O projeto usa objetos de comunicação para trocas de mensagens entre nós, como:
  - `WordSearchMessage`: Para realizar uma pesquisa por palavras-chave.
  - `FileSearchResult`: Contém o nome do ficheiro, o endereço, e porta do nó, hash e tamanho.
  - `FileBlockRequestMessage`: Solicita blocos específicos de um ficheiro.
  - `FileBlockAnswerMessage`: Encapsula os dados binários do bloco.
- **Sincronização e Gestão de Threads:** Um número máximo de threads (ex.: 5) é usado para limitar a carga sobre cada nó, utilizando uma `ThreadPool` para gerenciar os pedidos de descarregamento de ficheiros.
- **Gestão de Downloads:** Classe `DownloadTasksManager` para coordenar o download de blocos e garantir que a escrita do ficheiro para disco ocorre apenas após o download completo.

## Requisitos do Projeto

- **Sincronização:** Devem ser identificadas e protegidas as seções críticas para garantir a exclusão mútua de acessos.
- **Leitura da Pasta de Trabalho:** Ao iniciar, o programa deve ler a pasta de trabalho para listar os ficheiros disponíveis.
- **ThreadPool:** Os downloads devem ser geridos por uma `ThreadPool`, com um número máximo de threads definido.
- **Função de Hash:** Cada ficheiro tem um valor de hash SHA-256 associado, calculado com a classe `MessageDigest`.

## Execução

Execute a aplicação com o comando:

```bash
java IscTorrent <porta> <diretório_de_trabalho>
```
- `<porta>`: Porta que o nó usará para receber pedidos
- `<diretório_de_trabalho>`: Diretório onde se encontram os ficheiros partilhaods

## Exemplo de Utilização

1. Inicie a aplicação e conecte-se a outros nós da rede através da GUI.
2. Realize uma pesquisa por palavras-chave e visualize os resultados.
3. Selecione um ficheiro e inicie o descarregamento, que será feito por blocos de forma concorrente.
4. Após completar o download, o programa exibe o número de blocos recebidos de cada nó e o tempo total do descarregamento.
