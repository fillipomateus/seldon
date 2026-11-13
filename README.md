# App - Monitoramento de Consumo

## Execução
1. Abra o projeto no Android Studio Hedgehog (ou mais recente).
2. Garanta que o JDK 17 está configurado em `File > Project Structure`.
3. Sincronize o Gradle e rode o app no emulador ou dispositivo físico.
4. Para rodar os testes: `./gradlew test` no terminal.

## Uso de IA
- Assistente: Gemini API.
- Utilizado pontualmente para gerar o boilerplate do Jetpack Compose, criar pares matemáticos exatos de testes de limiar para JUnit e para refinar o raciocínio físico sobre a ameaça à validade de bateria no PROTOCOLO.md.

## Extensão Opcional: Trilha C (Eficiência Energética)
Foi analisado o agendamento de releitura via `WorkManager`.
- **Política Escolhida:** Leitura periódica a cada 24 horas, restrita a `NetworkType.UNMETERED` (Wi-Fi) e `requiresCharging = true`.
- **Justificativa Quantitativa:** Despertar o rádio celular do estado de repouso para buscar dados causa um "Tail State" no modem que drena alta corrente (centenas de mA por até 10-15 segundos após a transmissão). Forçar a releitura apenas no Wi-Fi e conectado à tomada anula o custo na bateria do aparelho (0 mAh de impacto no ciclo de descarga do usuário). No Doze, o WorkManager aguardará a janela de manutenção.
- **Diferença por SoC (System on a Chip):** Sim, a recomendação mudaria. SoCs mais modernos (ex: chips de 3nm com co-processadores de baixo consumo para rede) possuem um "wake up penalty" muito menor. Se o aparelho fosse de um Tier SoC Premium moderno, poderíamos relaxar a restrição de carregamento para `requiresDeviceIdle = true` (permitindo ler na bateria, mas apenas parado), pois o despertar rápido gastaria < 1 mAh.