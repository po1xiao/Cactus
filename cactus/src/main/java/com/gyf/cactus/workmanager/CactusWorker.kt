package com.gyf.cactus.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.gyf.cactus.ext.*
import com.gyf.cactus.other.CactusMsgEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * WorkManager定时器
 *
 * @author geyifeng
 * @date 2019-09-02 11:22
 */
class CactusWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    private val TAG = "CactusWorker"

    /**
     * 停止标识符
     */
    private var mIsStop = false

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        // context.registerStopReceiver {
        //     mIsStop = true
        // }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(messageEvent: CactusMsgEvent<Any>) {
        val code: Int = messageEvent.code
        if (code == CactusMsgEvent.SERVICE_STOP) {
            Log.d(TAG, "onEvent: SERVICE_STOP")
            mIsStop = true
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this)
            }
        }
    }

    override fun onStopped() {
        Log.d(TAG, "onStopped")
        super.onStopped()
    }


    override fun doWork(): Result {
        context.apply {
            val cactusConfig = getConfig()
            log("${this@CactusWorker}-doWork")
            if (!isCactusRunning && !mIsStop && !isStopped) {
                register(cactusConfig)
            }
        }
        return Result.success()
    }
}