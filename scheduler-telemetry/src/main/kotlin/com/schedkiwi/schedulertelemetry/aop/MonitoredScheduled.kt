package com.schedkiwi.schedulertelemetry.aop

/**
 * Anotação para marcar métodos de scheduler que devem ser monitorados por telemetria.
 * 
 * Esta anotação deve ser usada em conjunto com @Scheduled para ativar
 * a instrumentação automática de telemetria para o método.
 * 
 * Exemplo de uso:
 * ```kotlin
 * @Service
 * class MeuScheduler {
 *     
 *     @Scheduled(fixedRate = 60000)
 *     @MonitoredScheduled(jobId = "processamento-dados")
 *     fun processarDados() {
 *         // Lógica do scheduler aqui
 *     }
 * }
 * ```
 * 
 * @property jobId Identificador único do job para telemetria
 * @property description Descrição opcional do job
 * @property enableProgressTracking Se deve rastrear progresso em tempo real
 * @property progressUpdateInterval Intervalo de atualizações de progresso em ms
 * @property enablePerformanceMetrics Se deve coletar métricas de performance
 * @property customMetadata Metadados customizados para o job
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class MonitoredScheduled(
    
    /**
     * Identificador único do job para telemetria.
     * Este ID será usado para correlacionar relatórios e métricas.
     */
    val jobId: String,
    
    /**
     * Descrição opcional do job para facilitar identificação.
     */
    val description: String = "",
    
    /**
     * Se deve rastrear progresso em tempo real.
     * Quando habilitado, envia atualizações de progresso para o Gerenciador Central.
     */
    val enableProgressTracking: Boolean = true,
    
    /**
     * Intervalo de atualizações de progresso em milissegundos.
     * Só é usado se enableProgressTracking for true.
     */
    val progressUpdateInterval: Long = 1000L,
    
    /**
     * Se deve coletar métricas de performance detalhadas.
     * Inclui tempo de processamento, taxa de throughput, etc.
     */
    val enablePerformanceMetrics: Boolean = true,
    
    /**
     * Metadados customizados para o job.
     * Formato: ["chave1=valor1", "chave2=valor2"]
     */
    val customMetadata: Array<String> = [],
    
    /**
     * Se deve capturar stack traces completos de exceções.
     * Útil para debugging, mas pode impactar performance.
     */
    val captureFullStackTrace: Boolean = true,
    
    /**
     * Tamanho máximo do buffer de itens para evitar consumo excessivo de memória.
     */
    val maxItemBufferSize: Int = 10000,
    
    /**
     * Se deve registrar automaticamente o job no Gerenciador Central.
     */
    val autoRegister: Boolean = true,
    
    /**
     * Prioridade da mensagem para envio ao Gerenciador Central.
     * Valores: HIGH, NORMAL, LOW
     */
    val messagePriority: String = "NORMAL",
    
    /**
     * Tags adicionais para categorização e filtragem.
     */
    val tags: Array<String> = []
)
