package me.earthme.mysm.utils

import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object AsyncExecutor{
    private val threadId = AtomicInteger()

    val ASYNC_EXECUTOR_INSTANCE: ExecutorService = ThreadPoolExecutor(
        4,
        Integer.MAX_VALUE,
        30,
        TimeUnit.SECONDS,
        LinkedBlockingQueue(),
        ThreadFactory{
            val wrapped = Thread(it)
            wrapped.name = "MyYSM-Async-Worker-${threadId.getAndIncrement()}"
            wrapped.isDaemon = false
            wrapped.priority = 3
            return@ThreadFactory wrapped
        }
    )

    init {
        Runtime.getRuntime().addShutdownHook(Thread{
            ASYNC_EXECUTOR_INSTANCE.shutdownNow()
        })
    }
}