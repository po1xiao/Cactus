package com.gyf.cactus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.gyf.cactus.entity.Constant

/**
 * 注销服务监听
 *
 * @author geyifeng
 * @date 2019-11-19 10:05
 */
internal class StopReceiver private constructor(val context: Context) : BroadcastReceiver() {

    companion object {
        internal fun newInstance(context: Context) = StopReceiver(context)
    }

    /**
     * 待操作事件
     */
    private var mBlock: (() -> Unit)? = null

    private var mActionStop = "${Constant.CACTUS_FLAG_STOP}.${context.packageName}"

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(this, IntentFilter(mActionStop), RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(this, IntentFilter(mActionStop))
        }
    }

    /**
     * 注册
     *
     * @param block Function0<Unit>
     */
    internal fun register(block: () -> Unit) {
        mBlock = block
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.action?.also {
            when (it) {
                mActionStop -> {
                    this.context.unregisterReceiver(this)
                    mBlock?.let {
                        it()
                    }
                }
            }
        }
    }
}