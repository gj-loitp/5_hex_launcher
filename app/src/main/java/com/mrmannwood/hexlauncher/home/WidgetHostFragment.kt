package com.mrmannwood.hexlauncher.home

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.mrmannwood.hexlauncher.fragment.InstrumentedFragment
import com.mrmannwood.hexlauncher.measureScreen
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.FragmentHomeBinding

abstract class WidgetHostFragment : InstrumentedFragment() {

    private val viewModel: WidgetHostViewModel by activityViewModels()

    private lateinit var databinder: FragmentHomeBinding

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        databinder = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        databinder.adapter = HomeViewDatabindingAdapter
        databinder.description = makeDescription(isLoading = true)
        return databinder.root
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onViewCreated(databinder, savedInstanceState)

        viewModel.widgetsLiveData.forEach { liveData ->
            liveData.observe(
                viewLifecycleOwner,
                WidgetLiveDataObserver(view.context)
            )
        }
    }

    open fun onViewCreated(databinder: FragmentHomeBinding, savedInstanceState: Bundle?) {}

    open fun onWidgetLoaded(widgetView: View?, widgetName: String) {}

    fun onLoadingComplete() {
        databinder.description = makeDescription(false)
    }

    fun isLoading(): Boolean = databinder.description!!.isLoading()

    abstract fun makeDescription(isLoading: Boolean): HomeViewDescription

    private inner class WidgetLiveDataObserver(
        context: Context
    ) : Observer<WidgetHostViewModel.WidgetPlacement> {

        private val context = context.applicationContext
        private var widgetView: View? = null

        override fun onChanged(value: WidgetHostViewModel.WidgetPlacement) {
            if (!value.loaded) {
                return
            }
            if (value.yPosition == null || value.color == null) {
                hide()
            } else {
                show(value.layout, value.yPosition, value.xPosition, value.color)
            }
            onWidgetLoaded(widgetView, value.widget)
        }

        private fun show(@LayoutRes layout: Int, yPos: Float, xPos: Float?, color: Int) {
            val view = widgetView?.let { widget ->
                (widget.parent as? ViewGroup)?.removeView(widget)
                widget
            } ?: run {
                LayoutInflater.from(context).inflate(layout, databinder.container, false)
            }
            widgetView = view

            view.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.NO_GRAVITY
            )
            view.y = yPos
            if (xPos == null) {
                if (view.width == 0) {
                    view.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                        override fun onLayoutChange(
                            p0: View?,
                            p1: Int, p2: Int, p3: Int, p4: Int,
                            p5: Int, p6: Int, p7: Int, p8: Int
                        ) {
                            view.removeOnLayoutChangeListener(this)
                            show(layout, yPos, xPos, color)
                        }
                    })
                } else {
                    val screenWidth = measureScreen(requireActivity())
                    val x = (screenWidth / 2 - view.width / 2).toFloat()
                    view.x = x
                }
            } else {
                view.x = xPos
            }
            (view as TextView).setTextColor(color)
            databinder.container.addView(view)
        }

        private fun hide() {
            (widgetView?.parent as? ViewGroup)?.removeView(widgetView)
        }
    }
}
