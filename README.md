# Simulador de Redes Ópticas Elásticas - OpticalNetSim - Versão do Doutorado 2023

Este é um simulador de redes ópticas elásticas (EON) escrito em Java. Ele foi adaptado para melhorar a legibilidade e o desempenho em comparação com a versão original usada em um projeto de mestrado.

[SANTOS, Matheus Lôbo dos. Abordagens para atribuição de espectro em redes ópticas elásticas baseadas em perda de capacidade sob múltiplas rotas. 2021. Dissertação (Mestrado em Engenharia Elétrica) – Universidade Federal de Pernambuco, Recife, 2021.](https://repositorio.ufpe.br/handle/123456789/45594) 

## Funcionalidades

O simulador permite a criação de redes ópticas elásticas personalizadas e a simulação de fluxos de tráfego por meio dessas redes. Algumas das funcionalidades incluem:
- Criar topologias de rede com diferentes nós e enlaces
- Definir demandas de tráfego com diferentes requisitos de largura de banda e qualidade de serviço
- Alocar espectro óptico para as demandas usando diferentes algoritmos RMLSA (Routing and Modulation Level and Spectrum Assignment)
- Avaliar o desempenho da rede em termos de taxa de bloqueio, utilização do espectro e consumo energético

### Algoritmos Implementados
- Dijkstra:
- YEN:
- First-Fit:
- MSCLSequencial:
- MSCLCombinado:
- YEN por RSA:
- YEN por SAR:

Atualmente não implementada a camada física.

## Requisitos de instalação

Para instalar o simulador, siga os seguintes passos:

- Clone ou baixe este repositório para a sua máquina local
- Importe o projeto no Eclipse IDE ou similar
- Configure os parâmetros da simulação na classe <mark style="background-color: gray">ParametersSimulation.java</mark>
- Execute a classe Main.java

### Uso
Para usar o simulador, você precisa configurar os parâmetros da rede e das demandas no arquivo ParametersSimulation.java. Os parâmetros são:

- Número de nós da rede
- Número total de fibras por enlace
- Número total de demandas geradas
- Taxa média das demandas em bits por segundo
- Tempo médio entre chegadas das demandas em segundos
- Tempo médio de duração das demandas em segundos
- Algoritmo RMLSA usado na alocação do espectro
- Após configurar os parâmetros, execute a classe Main.java e aguarde os resultados. O simulador irá gerar gráficos e arquivos com as métricas da rede.

