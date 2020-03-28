package com.hieupt.ucropviewbox

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.setPadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.model.AspectRatio
import com.yalantis.ucrop.view.CropImageView
import com.yalantis.ucrop.view.OverlayView
import com.yalantis.ucrop.view.TransformImageView
import kotlinx.android.synthetic.main.ucrop_view_box_layout.view.*
import java.io.File
import java.util.*

/**
 * Created by HieuPT on 3/22/2020.
 */
class UCropViewBox : FrameLayout, LifecycleObserver {

    val gestureCropImageView
        get() = uCropView.cropImageView

    val overlayView
        get() = uCropView.overlayView

    private val transformImageListeners = mutableSetOf<TransformImageView.TransformImageListener>()

    var options = UCrop.Options()
        set(value) {
            field = value
            processOptions()
        }

    private val internalTransformListener = InternalTransformImageListener()

    private val imageTransformListener = object : SimpleTransformImageListener() {

        override fun onLoadComplete() {
            uCropView.animate().alpha(1f).setDuration(300).interpolator = AccelerateInterpolator()
            blockCropView(false)
        }
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet? = null, defStyleAttr: Int = 0) {
        View.inflate(context, R.layout.ucrop_view_box_layout, this)
        context.theme.obtainStyledAttributes(attrs, R.styleable.UCropViewBox, defStyleAttr, 0)
            .use { a ->
                overlayView.setPadding(
                    a.getDimensionPixelSize(
                        R.styleable.UCropViewBox_cropFramePadding,
                        context.resources.getDimensionPixelOffset(R.dimen.default_overlay_crop_frame_padding)
                    )
                )
            }
        blockCropView(true)
        setupCropImageView()
        addTransformImageListener(imageTransformListener)
    }

    private fun setupCropImageView() {
        gestureCropImageView.setTransformImageListener(internalTransformListener)
    }

    fun addTransformImageListener(transformImageListener: TransformImageView.TransformImageListener) {
        transformImageListeners.add(transformImageListener)
    }

    fun bindToLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun cancelAllAnimations() {
        gestureCropImageView.cancelAllAnimations()
    }

    fun withOptions(options: UCrop.Options) {
        this.options = options
    }

    @SuppressLint("PrivateResource")
    private fun processOptions() {
        val optionBundle = options.optionBundle
        // Crop image view options
        gestureCropImageView.apply {
            maxBitmapSize = optionBundle.getInt(
                UCrop.Options.EXTRA_MAX_BITMAP_SIZE,
                CropImageView.DEFAULT_MAX_BITMAP_SIZE
            )
            setMaxScaleMultiplier(
                optionBundle.getFloat(
                    UCrop.Options.EXTRA_MAX_SCALE_MULTIPLIER,
                    CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER
                )
            )
            setImageToWrapCropBoundsAnimDuration(
                optionBundle.getLong(
                    UCrop.Options.EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION,
                    CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION.toLong()
                )
            )
        }

        // Overlay view options
        overlayView.apply {
            optionBundle.getBoolean(
                UCrop.Options.EXTRA_FREE_STYLE_CROP,
                OverlayView.DEFAULT_FREESTYLE_CROP_MODE != OverlayView.FREESTYLE_CROP_MODE_DISABLE
            ).let { isEnabled ->
                freestyleCropMode =
                    if (isEnabled) OverlayView.FREESTYLE_CROP_MODE_ENABLE else OverlayView.FREESTYLE_CROP_MODE_DISABLE
            }
            setDimmedColor(
                optionBundle.getInt(
                    UCrop.Options.EXTRA_DIMMED_LAYER_COLOR,
                    ContextCompat.getColor(context, R.color.ucrop_color_default_dimmed)
                )
            )
            setCircleDimmedLayer(
                optionBundle.getBoolean(
                    UCrop.Options.EXTRA_CIRCLE_DIMMED_LAYER,
                    OverlayView.DEFAULT_CIRCLE_DIMMED_LAYER
                )
            )
            setShowCropFrame(
                optionBundle.getBoolean(
                    UCrop.Options.EXTRA_SHOW_CROP_FRAME,
                    OverlayView.DEFAULT_SHOW_CROP_FRAME
                )
            )
            setCropFrameColor(
                optionBundle.getInt(
                    UCrop.Options.EXTRA_CROP_FRAME_COLOR,
                    ContextCompat.getColor(context, R.color.ucrop_color_default_crop_frame)
                )
            )
            setCropFrameStrokeWidth(
                optionBundle.getInt(
                    UCrop.Options.EXTRA_CROP_FRAME_STROKE_WIDTH,
                    resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_frame_stoke_width)
                )
            )
            setShowCropGrid(
                optionBundle.getBoolean(
                    UCrop.Options.EXTRA_SHOW_CROP_GRID,
                    OverlayView.DEFAULT_SHOW_CROP_GRID
                )
            )
            setCropGridRowCount(
                optionBundle.getInt(
                    UCrop.Options.EXTRA_CROP_GRID_ROW_COUNT,
                    OverlayView.DEFAULT_CROP_GRID_ROW_COUNT
                )
            )
            setCropGridColumnCount(
                optionBundle.getInt(
                    UCrop.Options.EXTRA_CROP_GRID_COLUMN_COUNT,
                    OverlayView.DEFAULT_CROP_GRID_COLUMN_COUNT
                )
            )
            setCropGridColor(
                optionBundle.getInt(
                    UCrop.Options.EXTRA_CROP_GRID_COLOR,
                    ContextCompat.getColor(context, R.color.ucrop_color_default_crop_grid)
                )
            )
            setCropGridStrokeWidth(
                optionBundle.getInt(
                    UCrop.Options.EXTRA_CROP_GRID_STROKE_WIDTH,
                    resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_grid_stoke_width)
                )
            )
        }

        // Aspect ratio options
        val aspectRatioX: Float = optionBundle.getFloat(UCrop.EXTRA_ASPECT_RATIO_X, 0f)
        val aspectRatioY: Float = optionBundle.getFloat(UCrop.EXTRA_ASPECT_RATIO_Y, 0f)

        val aspectRationSelectedByDefault: Int =
            optionBundle.getInt(UCrop.Options.EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, 0)
        val aspectRatioList: ArrayList<AspectRatio>? =
            optionBundle.getParcelableArrayList(UCrop.Options.EXTRA_ASPECT_RATIO_OPTIONS)

        if (aspectRatioX > 0 && aspectRatioY > 0) {
            gestureCropImageView.targetAspectRatio = aspectRatioX / aspectRatioY
        } else if (aspectRatioList != null && aspectRationSelectedByDefault < aspectRatioList.size) {
            gestureCropImageView.targetAspectRatio =
                aspectRatioList[aspectRationSelectedByDefault].aspectRatioX /
                        aspectRatioList[aspectRationSelectedByDefault].aspectRatioY
        } else {
            gestureCropImageView.targetAspectRatio = CropImageView.SOURCE_IMAGE_ASPECT_RATIO
        }

        // Result bitmap max size options
        val maxSizeX: Int = optionBundle.getInt(UCrop.EXTRA_MAX_SIZE_X, 0)
        val maxSizeY: Int = optionBundle.getInt(UCrop.EXTRA_MAX_SIZE_Y, 0)

        if (maxSizeX > 0 && maxSizeY > 0) {
            gestureCropImageView.setMaxResultImageSizeX(maxSizeX)
            gestureCropImageView.setMaxResultImageSizeY(maxSizeY)
        }
    }

    private fun resetCropView() {
        uCropView.resetCropImageView()
        setupCropImageView()
    }

    private fun blockCropView(isBlocking: Boolean) {
        blockingView.isClickable = isBlocking
    }

    fun setImageUri(inputUri: Uri, outputUri: Uri? = null) {
        blockCropView(true)
        val outUri = if (outputUri != null) {
            outputUri
        } else {
            val destinationFileName = SAMPLE_CROPPED_IMAGE_NAME
            Uri.fromFile(File(uCropView.context.cacheDir, destinationFileName))
        }
        try {
            resetCropView()
            processOptions()
            gestureCropImageView.setImageUri(inputUri, outUri)
        } catch (e: Exception) {
        }
    }

    fun setTargetAspectRatio(targetAspectRatio: Float) {
        gestureCropImageView.apply {
            this.targetAspectRatio = targetAspectRatio
            options.withAspectRatio(targetAspectRatio, 1f)
            setImageToWrapCropBounds()
        }
    }

    fun setTargetAspectRatio(aspectRatioX: Float, aspectRatioY: Float) {
        setTargetAspectRatio(aspectRatioX / aspectRatioY)
    }

    private fun resetRotation() {
        gestureCropImageView.apply {
            postRotate(-gestureCropImageView.currentAngle)
            setImageToWrapCropBounds()
        }
    }

    private fun resetZoom() {
        gestureCropImageView.zoomOutImage(gestureCropImageView.minScale)
    }

    fun cropAndSaveImage(
        compressFormat: Bitmap.CompressFormat = DEFAULT_COMPRESS_FORMAT,
        compressQuality: Int = DEFAULT_COMPRESS_QUALITY,
        callback: BitmapCropCallback
    ) {
        blockCropView(true)
        gestureCropImageView.cropAndSaveImage(
            compressFormat,
            compressQuality,
            object : ProxyBitmapCropCallback(callback) {
                override fun onBitmapCropped(
                    resultUri: Uri,
                    offsetX: Int,
                    offsetY: Int,
                    imageWidth: Int,
                    imageHeight: Int
                ) {
                    blockCropView(false)
                    super.onBitmapCropped(resultUri, offsetX, offsetY, imageWidth, imageHeight)
                }

                override fun onCropFailure(t: Throwable) {
                    blockCropView(false)
                    super.onCropFailure(t)
                }
            })
    }

    private fun rotateByAngle(angle: Int) {
        gestureCropImageView.apply {
            postRotate(angle.toFloat())
            setImageToWrapCropBounds()
        }
    }

    inner class InternalTransformImageListener : TransformImageView.TransformImageListener {

        override fun onRotate(currentAngle: Float) {
            transformImageListeners.forEach { it.onRotate(currentAngle) }
        }

        override fun onLoadComplete() {
            transformImageListeners.forEach { it.onLoadComplete() }
        }

        override fun onScale(currentScale: Float) {
            transformImageListeners.forEach { it.onScale(currentScale) }
        }

        override fun onLoadFailure(e: Exception) {
            transformImageListeners.forEach { it.onLoadFailure(e) }
        }
    }

    companion object {
        private const val SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage.png"
        private const val DEFAULT_COMPRESS_QUALITY = 90
        private val DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG
    }
}