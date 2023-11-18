package me.earthme.mysm.utils

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

object AsyncExecutor{
    private val threadId = AtomicInteger()

    //TODO Use a fixed thread pool?
    val ASYNC_EXECUTOR_INSTANCE: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()) {
        val wrapped = Executors.defaultThreadFactory().newThread(it)
        wrapped.name = "MyYSM-Async-Worker-${threadId.getAndIncrement()}"
        wrapped.isDaemon = true
        wrapped.priority = 3
        return@newFixedThreadPool wrapped
    }

    init {
        Runtime.getRuntime().addShutdownHook(Thread{
            ASYNC_EXECUTOR_INSTANCE.shutdownNow()
        })
    }
}