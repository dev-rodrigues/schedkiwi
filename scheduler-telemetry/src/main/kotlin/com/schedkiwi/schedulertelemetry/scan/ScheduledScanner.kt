package com.schedkiwi.schedulertelemetry.scan

import com.schedkiwi.schedulertelemetry.aop.MonitoredScheduled
import com.schedkiwi.schedulertelemetry.net.ScheduledJobInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * Scanner para descobrir automaticamente jobs agendados na aplicação.
 * 
 * Este componente varre o ApplicationContext para encontrar métodos
 * anotados com @Scheduled e @MonitoredScheduled, coletando informações
 * sobre cron expressions, intervalos e configurações.
 */
@Component
class ScheduledScanner @Autowired constructor(
    private val applicationContext: ApplicationContext
) {
    
    private val logger = LoggerFactory.getLogger(ScheduledScanner::class.java)
    
    /**
     * Cache de jobs descobertos para evitar re-scanning desnecessário
     */
    private val discoveredJobs = ConcurrentHashMap<String, ScheduledJobInfo>()
    
    /**
     * Flag para controlar se o scanner está ativo
     */
    private var isScanning = false
    
    /**
     * Último timestamp de scan
     */
    private var lastScanTime: Long = 0L
    
    /**
     * Intervalo mínimo entre scans (em ms)
     */
    private val minScanIntervalMs = 5000L
    
    /**
     * Descobre todos os jobs agendados na aplicação
     */
    fun discoverScheduledJobs(): List<ScheduledJobInfo> {
        if (isScanning) {
            logger.debug("Scan já em andamento, retornando cache")
            return discoveredJobs.values.toList()
        }
        
        val now = System.currentTimeMillis()
        if (now - lastScanTime < minScanIntervalMs) {
            logger.debug("Intervalo mínimo entre scans não atingido, retornando cache")
            return discoveredJobs.values.toList()
        }
        
        return performScan()
    }
    
    /**
     * Executa o scan real dos jobs
     */
    private fun performScan(): List<ScheduledJobInfo> {
        isScanning = true
        lastScanTime = System.currentTimeMillis()
        
        try {
            logger.info("Iniciando scan de jobs agendados")
            
            // Limpa cache anterior
            discoveredJobs.clear()
            
            // Obtém todos os beans do ApplicationContext
            val beanNames = applicationContext.beanDefinitionNames
            
            for (beanName in beanNames) {
                try {
                    val bean = applicationContext.getBean(beanName)
                    scanBeanForScheduledMethods(bean, beanName)
                } catch (e: Exception) {
                    logger.warn("Erro ao escanear bean: {}", beanName, e)
                }
            }
            
            logger.info("Scan concluído: {} jobs descobertos", discoveredJobs.size)
            return discoveredJobs.values.toList()
            
        } finally {
            isScanning = false
        }
    }
    
    /**
     * Escaneia um bean específico em busca de métodos agendados
     */
    private fun scanBeanForScheduledMethods(bean: Any, beanName: String) {
        val beanClass = bean.javaClass
        
        // Verifica se a classe tem métodos anotados
        if (!hasScheduledAnnotations(beanClass)) {
            return
        }
        
        val methods = beanClass.declaredMethods
        
        for (method in methods) {
            try {
                scanMethodForScheduling(method, bean, beanName)
            } catch (e: Exception) {
                logger.warn("Erro ao escanear método: {}.{}", beanName, method.name, e)
            }
        }
    }
    
    /**
     * Verifica se uma classe tem anotações de scheduling
     */
    private fun hasScheduledAnnotations(clazz: Class<*>): Boolean {
        return clazz.declaredMethods.any { method ->
            method.isAnnotationPresent(Scheduled::class.java) ||
            method.isAnnotationPresent(MonitoredScheduled::class.java)
        }
    }
    
    /**
     * Escaneia um método específico em busca de anotações de scheduling
     */
    private fun scanMethodForScheduling(method: Method, bean: Any, beanName: String) {
        val scheduledAnnotation = method.getAnnotation(Scheduled::class.java)
        val monitoredAnnotation = method.getAnnotation(MonitoredScheduled::class.java)
        
        if (scheduledAnnotation == null && monitoredAnnotation == null) {
            return
        }
        
        // Determina o jobId
        val jobId = when {
            monitoredAnnotation != null -> monitoredAnnotation.jobId
            scheduledAnnotation != null -> generateJobId(beanName, method.name)
            else -> generateJobId(beanName, method.name)
        }
        
        // Extrai informações de scheduling
        val schedulingInfo = extractSchedulingInfo(scheduledAnnotation)
        
        // Cria ScheduledJobInfo
        val jobInfo = ScheduledJobInfo(
            jobId = jobId,
            methodName = method.name,
            className = bean.javaClass.simpleName,
            cronExpression = schedulingInfo.cronExpression,
            fixedRate = schedulingInfo.fixedRate,
            fixedDelay = schedulingInfo.fixedDelay,
            initialDelay = schedulingInfo.initialDelay,
            timeUnit = schedulingInfo.timeUnit.name,
            isMonitored = monitoredAnnotation != null,
            description = monitoredAnnotation?.description ?: "",
            tags = monitoredAnnotation?.tags?.toList() ?: emptyList()
        )
        
        discoveredJobs[jobId] = jobInfo
        
        logger.debug("Job descoberto: jobId={}, class={}, method={}, cron={}", 
            jobId, bean.javaClass.simpleName, method.name, schedulingInfo.cronExpression)
            
    }
    
    /**
     * Extrai informações de scheduling de uma anotação @Scheduled
     */
    private fun extractSchedulingInfo(scheduledAnnotation: Scheduled?): SchedulingInfo {
        if (scheduledAnnotation == null) {
            return SchedulingInfo()
        }
        
        return SchedulingInfo(
            cronExpression = scheduledAnnotation.cron.takeIf { it.isNotEmpty() },
            fixedRate = scheduledAnnotation.fixedRate.takeIf { it > 0 },
            fixedDelay = scheduledAnnotation.fixedDelay.takeIf { it > 0 },
            initialDelay = scheduledAnnotation.initialDelay.takeIf { it > 0 },
            timeUnit = scheduledAnnotation.timeUnit
        )
    }
    
    /**
     * Gera um jobId único baseado no bean e método
     */
    private fun generateJobId(beanName: String, methodName: String): String {
        return "${beanName}_${methodName}".lowercase()
            .replace("[^a-z0-9_]".toRegex(), "_")
            .replace("_+".toRegex(), "_")
            .trim('_')
    }
    
    /**
     * Obtém informações de um job específico
     */
    fun getJobInfo(jobId: String): ScheduledJobInfo? {
        return discoveredJobs[jobId]
    }
    
    /**
     * Obtém todos os jobs descobertos
     */
    fun getAllJobs(): List<ScheduledJobInfo> {
        return discoveredJobs.values.toList()
    }
    
    /**
     * Obtém apenas jobs monitorados
     */
    fun getMonitoredJobs(): List<ScheduledJobInfo> {
        return discoveredJobs.values.filter { it.isMonitored }
    }
    
    /**
     * Obtém apenas jobs não monitorados
     */
    fun getUnmonitoredJobs(): List<ScheduledJobInfo> {
        return discoveredJobs.values.filter { !it.isMonitored }
    }
    
    /**
     * Força um novo scan (ignorando cache)
     */
    fun forceRescan(): List<ScheduledJobInfo> {
        discoveredJobs.clear()
        lastScanTime = 0L
        return discoverScheduledJobs()
    }
    
    /**
     * Obtém estatísticas do scanner
     */
    fun getScannerStats(): ScannerStats {
        return ScannerStats(
            totalJobs = discoveredJobs.size,
            monitoredJobs = discoveredJobs.values.count { it.isMonitored },
            unmonitoredJobs = discoveredJobs.values.count { !it.isMonitored },
            lastScanTime = lastScanTime,
            isScanning = isScanning,
            cacheSize = discoveredJobs.size
        )
    }
    
    /**
     * Limpa o cache de jobs descobertos
     */
    fun clearCache() {
        discoveredJobs.clear()
        lastScanTime = 0L
        logger.info("Cache de jobs limpo")
    }
}

/**
 * Informações de scheduling extraídas de @Scheduled
 */
data class SchedulingInfo(
    val cronExpression: String? = null,
    val fixedRate: Long? = null,
    val fixedDelay: Long? = null,
    val initialDelay: Long? = null,
    val timeUnit: java.util.concurrent.TimeUnit = java.util.concurrent.TimeUnit.MILLISECONDS
)

/**
 * Estatísticas do scanner
 */
data class ScannerStats(
    val totalJobs: Int,
    val monitoredJobs: Int,
    val unmonitoredJobs: Int,
    val lastScanTime: Long,
    val isScanning: Boolean,
    val cacheSize: Int
)
