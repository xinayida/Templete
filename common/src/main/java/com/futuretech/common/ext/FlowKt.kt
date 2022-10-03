@file:Suppress("unused")

package com.futuretech.common.ext

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

/**
 * 仅发送一个
 */
fun <T> SendChannel<T>.trySendOnlyOne(data: T) {
    trySend(data)
    close()
}

/**
 * callbackFlow 自动 awaitClose
 */
fun <T> callbackFlowAutoAwaitClose(block: suspend ProducerScope<T>.() -> Unit): Flow<T> =
    callbackFlow {
        block()
        awaitClose()
    }

/**
 * asFlow
 */
fun <T> T.asFlow(): Flow<T> {
    return flowOf(this)
}

/**
 * Flow 安全版自动捕获异常的 emit()
 *
 * @param defaultValue 默认值，若未设置默认值，错误后就会抛出异常，设置了错误后就会使用默认值
 */
suspend fun <T> FlowCollector<T>.emitDefaultWhenError(
    defaultValue: T? = null,
    value: suspend () -> T
) {
    var error: Throwable? = null
    val result = runCatching { value() }.onFailure {
        error = it
        it.printStackTrace()
    }.getOrNull() ?: defaultValue ?: throw Exception(error)
    emit(result)
}


fun <O, T1, R> Flow<O>.zipMore(
    other1: Flow<T1>,
    transform: suspend (O, T1) -> R
): Flow<R> {
    return this
        .zip(other1) { data, append ->
            return@zip transform.invoke(data, append)
        }
}

fun <O, T1, T2, R> Flow<O>.zipMore(
    other1: Flow<T1>,
    other2: Flow<T2>,
    transform: suspend (O, T1, T2) -> R
): Flow<R> {
    return this
        .zip(other1) { orgData, append ->
            return@zip Polytechnic2(orgData, append)
        }
        .zip(other2) { data: Polytechnic2<O, T1>, append ->
            return@zip transform.invoke(data.dataA, data.dataB, append)
        }
}

fun <O, T1, T2, T3, R> Flow<O>.zipMore(
    other1: Flow<T1>,
    other2: Flow<T2>,
    other3: Flow<T3>,
    transform: suspend (O, T1, T2, T3) -> R
): Flow<R> {
    return with(this) {
        zip(other1) { data, append ->
            return@zip Polytechnic2(
                data,
                append
            )
        }.zip(other2) { data, append ->
            return@zip Polytechnic3(
                data.dataA,
                data.dataB,
                append
            )
        }.zip(other3) { data, append ->
            return@zip transform.invoke(
                data.dataA,
                data.dataB,
                data.dataC,
                append
            )
        }
    }
}


fun <O, T1, T2, T3, T4, R> Flow<O>.zipMore(
    other1: Flow<T1>,
    other2: Flow<T2>,
    other3: Flow<T3>,
    other4: Flow<T4>,
    transform: suspend (O, T1, T2, T3, T4) -> R
): Flow<R> {
    return with(this) {
        zip(other1) { data, append ->
            return@zip Polytechnic2(
                data,
                append
            )
        }.zip(other2) { data, append ->
            return@zip Polytechnic3(
                data.dataA,
                data.dataB,
                append
            )
        }.zip(other3) { data, append ->
            return@zip Polytechnic4(
                data.dataA,
                data.dataB,
                data.dataC,
                append
            )
        }.zip(other4) { data, append ->
            return@zip transform.invoke(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                append
            )
        }
    }
}

fun <O, T1, T2, T3, T4, T5, R> Flow<O>.zipMore(
    other1: Flow<T1>,
    other2: Flow<T2>,
    other3: Flow<T3>,
    other4: Flow<T4>,
    other5: Flow<T5>,
    transform: suspend (O, T1, T2, T3, T4, T5) -> R
): Flow<R> {
    return with(this) {
        zip(other1) { data, append ->
            return@zip Polytechnic2(
                data,
                append
            )
        }.zip(other2) { data, append ->
            return@zip Polytechnic3(
                data.dataA,
                data.dataB,
                append
            )
        }.zip(other3) { data, append ->
            return@zip Polytechnic4(
                data.dataA,
                data.dataB,
                data.dataC,
                append
            )
        }.zip(other4) { data, append ->
            return@zip Polytechnic5(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                append
            )
        }.zip(other5) { data, append ->
            return@zip transform.invoke(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                data.dataE,
                append
            )
        }
    }
}

fun <O, T1, T2, T3, T4, T5, T6, R> Flow<O>.zipMore(
    other1: Flow<T1>,
    other2: Flow<T2>,
    other3: Flow<T3>,
    other4: Flow<T4>,
    other5: Flow<T5>,
    other6: Flow<T6>,
    transform: suspend (O, T1, T2, T3, T4, T5, T6) -> R
): Flow<R> {
    return with(this) {
        zip(other1) { data, append ->
            return@zip Polytechnic2(
                data,
                append
            )
        }.zip(other2) { data, append ->
            return@zip Polytechnic3(
                data.dataA,
                data.dataB,
                append
            )
        }.zip(other3) { data, append ->
            return@zip Polytechnic4(
                data.dataA,
                data.dataB,
                data.dataC,
                append
            )
        }.zip(other4) { data, append ->
            return@zip Polytechnic5(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                append
            )
        }.zip(other5) { data, append ->
            return@zip Polytechnic6(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                data.dataE,
                append
            )
        }.zip(other6) { data, append ->
            return@zip transform.invoke(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                data.dataE,
                data.dataF,
                append
            )
        }
    }
}

fun <O, T1, T2, T3, T4, T5, T6, T7, R> Flow<O>.zipMore(
    other1: Flow<T1>,
    other2: Flow<T2>,
    other3: Flow<T3>,
    other4: Flow<T4>,
    other5: Flow<T5>,
    other6: Flow<T6>,
    other7: Flow<T7>,
    transform: suspend (O, T1, T2, T3, T4, T5, T6, T7) -> R
): Flow<R> {
    return with(this) {
        zip(other1) { data, append ->
            return@zip Polytechnic2(
                data,
                append
            )
        }.zip(other2) { data, append ->
            return@zip Polytechnic3(
                data.dataA,
                data.dataB,
                append
            )
        }.zip(other3) { data, append ->
            return@zip Polytechnic4(
                data.dataA,
                data.dataB,
                data.dataC,
                append
            )
        }.zip(other4) { data, append ->
            return@zip Polytechnic5(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                append
            )
        }.zip(other5) { data, append ->
            return@zip Polytechnic6(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                data.dataE,
                append
            )
        }.zip(other6) { data, append ->
            return@zip Polytechnic7(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                data.dataE,
                data.dataF,
                append
            )
        }.zip(other7) { data, append ->
            return@zip transform.invoke(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                data.dataE,
                data.dataF,
                data.dataG,
                append
            )
        }
    }
}


fun <O, T1, T2, T3, T4, T5, T6, T7, T8, R> Flow<O>.zipMore(
    other1: Flow<T1>,
    other2: Flow<T2>,
    other3: Flow<T3>,
    other4: Flow<T4>,
    other5: Flow<T5>,
    other6: Flow<T6>,
    other7: Flow<T7>,
    other8: Flow<T8>,
    transform: suspend (O, T1, T2, T3, T4, T5, T6, T7, T8) -> R
): Flow<R> {
    return with(this) {
        zip(other1) { data, append ->
            return@zip Polytechnic2(
                data,
                append
            )
        }.zip(other2) { data, append ->
            return@zip Polytechnic3(
                data.dataA,
                data.dataB,
                append
            )
        }.zip(other3) { data, append ->
            return@zip Polytechnic4(
                data.dataA,
                data.dataB,
                data.dataC,
                append
            )
        }.zip(other4) { data, append ->
            return@zip Polytechnic5(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                append
            )
        }.zip(other5) { data, append ->
            return@zip Polytechnic6(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                data.dataE,
                append
            )
        }.zip(other6) { data, append ->
            return@zip Polytechnic7(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                data.dataE,
                data.dataF,
                append
            )
        }.zip(other7) { data, append ->
            return@zip Polytechnic8(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                data.dataE,
                data.dataF,
                data.dataG,
                append
            )
        }.zip(other8) { data, append ->
            return@zip transform.invoke(
                data.dataA,
                data.dataB,
                data.dataC,
                data.dataD,
                data.dataE,
                data.dataF,
                data.dataG,
                data.dataH,
                append
            )
        }
    }
}

