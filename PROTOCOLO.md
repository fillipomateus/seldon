# Protocolo de Diagnóstico e Gestão de Frota

## 1. Regra de Classificação Adotada
A classificação dos dispositivos segue estritamente a arquitetura limpa e independente da plataforma Android. O cálculo do dreno baseia-se na normalização das unidades de consumo:

* **Conversão de mWh para mAh:** `mAh = mWh / nominalVoltage`
* **Dreno Absoluto (mAh/h):** `Dreno Absoluto = energyConsumedMah / (screenOnMinutes / 60)`
* **Dreno Relativo (%/h):** `Dreno Relativo = (Dreno Absoluto / batteryCapacityMah) * 100`

---

### Limiares de Decisão

1. **CRÍTICO:**
    * Dreno Relativo ≥ 12 %/h **E**
    * Falhas (7d) ≥ 3 **E**
    * Quadros Perdidos ≥ 15%

2. **ATENÇÃO:**
    * Dreno Relativo ≥ 7 %/h **E**
    * Falhas (7d) ≥ 1 **E**
    * Quadros Perdidos ≥ 8%

3. **OK:**
    * Nenhuma das condições anteriores atingida.

---

## 2. Tratamento de Defeitos e Ingestão de Dados
Registros com dados corrompidos ou incompletos (capacidade de bateria ≤ 0, tempo de tela ligada ≤ 0 ou ausência de tensão nominal para dados em mWh) são descartados na camada de dados (`DeviceRepositoryImpl`). Essa filtragem previne erros matemáticos como divisão por zero, `NaN` e `Infinity` na camada de domínio.

---

## 3. Ameaça Concreta à Validade
**Assunção de Dreno de Tela Ativa:** A métrica de dreno relativo assume que todo o consumo de energia informado ocorreu exclusivamente durante o período em que a tela esteve ligada (`screenOnMinutes`).

> **Ameaça:** Processos em segundo plano ou consumo no modo *standby* durante longos períodos com a tela desligada inflacionam a taxa aparente de dreno por hora, podendo classificar erroneamente um aparelho como **ATENÇÃO** ou **CRÍTICO** devido ao consumo passivo de fundo.