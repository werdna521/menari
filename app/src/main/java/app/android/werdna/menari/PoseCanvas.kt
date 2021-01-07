package app.android.werdna.menari

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import java.lang.Math.max
import java.lang.Math.min
import java.util.Locale

/** Draw the detected pose in preview.  */
class PoseGraphic internal constructor(
    overlay: GraphicOverlay,
    private val pose: Pose,
    private val showInFrameLikelihood: Boolean,
    private val imageProxy: ImageProxy,
    private val viewWidth: Int,
    private val viewHeight: Int
) :
    GraphicOverlay.Graphic(overlay) {
    private val leftPaint: Paint
    private val rightPaint: Paint
    private val redPaint: Paint
    private var multiplierX: Float = 0f
    private var multiplierY: Float = 0f

    private fun getMultiplierX(): Float {
        val width =
            if (imageProxy.imageInfo.rotationDegrees % 180 == 0) imageProxy.width else imageProxy.height
        return viewWidth.toFloat() / width.toFloat()
    }

    private fun getMultiplierY(): Float {
        val height =
            if (imageProxy.imageInfo.rotationDegrees % 180 == 0) imageProxy.height else imageProxy.width
        return viewHeight.toFloat() / height.toFloat()
    }

    init {
        multiplierX = getMultiplierX()
        multiplierY = getMultiplierY()
    }

    override fun draw(canvas: Canvas?) {
        val landmarks =
            pose.allPoseLandmarks
        if (landmarks.isEmpty()) {
            return
        }
        // Draw all the points
        for (landmark in landmarks) {
            drawPoint(canvas!!, landmark, redPaint)
        }
        val leftShoulder =
            pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder =
            pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow =
            pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow =
            pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist =
            pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist =
            pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftHip =
            pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip =
            pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftKnee =
            pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val rightKnee =
            pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val leftAnkle =
            pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle =
            pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
        val leftPinky =
            pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)
        val rightPinky =
            pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
        val leftIndex =
            pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
        val rightIndex =
            pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
        val leftThumb =
            pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)
        val rightThumb =
            pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
        val leftHeel =
            pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
        val rightHeel =
            pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
        val leftFootIndex =
            pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
        val rightFootIndex =
            pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)
        drawLine(canvas!!, leftShoulder!!, rightShoulder!!, redPaint)
        drawLine(canvas, leftHip!!, rightHip!!, redPaint)
        // Left body
        drawLine(canvas, leftShoulder, leftElbow!!, leftPaint)
        drawLine(canvas, leftElbow, leftWrist!!, leftPaint)
        drawLine(canvas, leftShoulder, leftHip, leftPaint)
        drawLine(canvas, leftHip, leftKnee!!, leftPaint)
        drawLine(canvas, leftKnee, leftAnkle!!, leftPaint)
        drawLine(canvas, leftWrist, leftThumb!!, leftPaint)
        drawLine(canvas, leftWrist, leftPinky!!, leftPaint)
        drawLine(canvas, leftWrist, leftIndex!!, leftPaint)
        drawLine(canvas, leftAnkle, leftHeel!!, leftPaint)
        drawLine(canvas, leftHeel, leftFootIndex!!, leftPaint)
        // Right body
        drawLine(canvas, rightShoulder, rightElbow!!, rightPaint)
        drawLine(canvas, rightElbow, rightWrist!!, rightPaint)
        drawLine(canvas, rightShoulder, rightHip, rightPaint)
        drawLine(canvas, rightHip, rightKnee!!, rightPaint)
        drawLine(canvas, rightKnee, rightAnkle!!, rightPaint)
        drawLine(canvas, rightWrist, rightThumb!!, rightPaint)
        drawLine(canvas, rightWrist, rightPinky!!, rightPaint)
        drawLine(canvas, rightWrist, rightIndex!!, rightPaint)
        drawLine(canvas, rightAnkle, rightHeel!!, rightPaint)
        drawLine(canvas, rightHeel, rightFootIndex!!, rightPaint)
    }

    fun drawPoint(canvas: Canvas, landmark: PoseLandmark, paint: Paint) {
        val point = landmark.position
        canvas.drawCircle(
            translateX(multiplierX * point.x),
            translateY(multiplierY * point.y),
            DOT_RADIUS,
            paint
        )
    }

    fun drawLine(
        canvas: Canvas,
        startLandmark: PoseLandmark,
        endLandmark: PoseLandmark,
        paint: Paint
    ) {
        val start = startLandmark.position
        val end = endLandmark.position
        canvas.drawLine(
            translateX(multiplierX * start.x),
            translateY(multiplierY * start.y),
            translateX(multiplierX * end.x),
            translateY(multiplierY * end.y),
            paint
        )
    }

    companion object {
        private const val DOT_RADIUS = 8.0f
        private const val IN_FRAME_LIKELIHOOD_TEXT_SIZE = 30.0f
    }

    init {
        redPaint = Paint()
        redPaint.color = Color.RED
        leftPaint = Paint()
        leftPaint.color = Color.GREEN
        rightPaint = Paint()
        rightPaint.color = Color.YELLOW
    }
}