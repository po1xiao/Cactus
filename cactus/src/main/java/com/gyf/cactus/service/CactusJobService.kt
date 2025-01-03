package com.gyf.cactus.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.gyf.cactus.entity.CactusConfig
import com.gyf.cactus.entity.Constant
import com.gyf.cactus.ext.*
import com.gyf.cactus.other.CactusMsgEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author geyifeng
 * @date 2019-08-30 13:03
 */
class CactusJobService : JobService() {
    private val TAG = "CactusJobService"
    private lateinit var mJobScheduler: JobScheduler

    private lateinit var mCactusConfig: CactusConfig

    private var mJobId = 100

    /**
     * 停止标识符
     */
    private var mIsStop = false

    override fun onCreate() {
        super.onCreate()
        mCactusConfig = getConfig()
        registerJob()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        Log.d(TAG, "onCreate")
        // registerStopReceiver {
        //     mIsStop = true
        //     stopService()
        // }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getParcelableExtra<CactusConfig>(Constant.CACTUS_CONFIG)?.let {
            mCactusConfig = it
        }
        setNotification(mCactusConfig.notificationConfig)
        registerCactus(mCactusConfig)
        return START_STICKY
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(messageEvent: CactusMsgEvent<Any>) {
        val code: Int = messageEvent.code
        if (code == CactusMsgEvent.SERVICE_STOP) {
            Log.d(TAG, "onEvent: SERVICE_STOP")
            mIsStop = true
            stopService()
        }
    }

    override fun onDestroy() {
        stopForeground(true)
        mJobScheduler.cancel(mJobId)
        saveJobId(-1)
        Log.d(TAG, "onDestroy")
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onDestroy()
    }

    override fun onStartJob(jobParameters: JobParameters): Boolean {
        log("onStartJob")
        if (!isCactusRunning && !mIsStop) {
            registerCactus(mCactusConfig)
        }
        return false
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        log("onStopJob")
        if (!isCactusRunning && !mIsStop) {
            registerCactus(mCactusConfig)
        }
        return false
    }

    /**
     * 开始Job
     */
    private fun registerJob() {
        mJobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        mJobId = getJobId()
        if (mJobId != -1) {
            mJobScheduler.cancel(mJobId)
        }
        mJobId = id
        saveJobId(mJobId)
        val builder = JobInfo.Builder(
            mJobId,
            ComponentName(packageName, CactusJobService::class.java.name)
        ).apply {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setMinimumLatency(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS) // 执行的最小延迟时间
                    setOverrideDeadline(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS)  // 执行的最长延时时间
                    setMinimumLatency(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS)
                    setBackoffCriteria(
                        JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS,
                        JobInfo.BACKOFF_POLICY_LINEAR
                    )// 线性重试方案
                } else {
                    setPeriodic(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS)
                    setRequiresDeviceIdle(true)
                }
                setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                setRequiresCharging(true) // 当插入充电器，执行该任务
                setPersisted(true)
            } catch (e: Exception) {
            }
        }
        mJobScheduler.schedule(builder.build())
    }
}