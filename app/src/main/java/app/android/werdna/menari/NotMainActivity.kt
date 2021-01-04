package app.android.werdna.menari

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.android.synthetic.main.activity_not_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

class NotMainActivity : AppCompatActivity() {

    private var trainImageList: List<File> = listOf()
    private var realImageList: List<File> = listOf()
    private var index = -1
    private var i = 0
    private lateinit var poseDetector: PoseDetector
    private lateinit var path: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_not_main)

        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
            .build()

        poseDetector = PoseDetection.getClient(options)

        init(true)

        perfect.setOnClickListener(View.OnClickListener {
            inference(1, true, null)
        })

        good.setOnClickListener(View.OnClickListener {
            inference(2, true, null)
        })

        bad.setOnClickListener(View.OnClickListener {
            inference(3, true, null)
        })

        reset.setOnClickListener(View.OnClickListener {
            resetIndex()
        })

        Toast.makeText(this, "HA PENCET LA KAU", Toast.LENGTH_LONG).show()
    }

    private fun init(pre: Boolean) {
        index = getCurrentIndex()

        path = getExternalFilesDir(null)?.absolutePath ?: "gilak"
        if (path == "gilak") {
            return Toast.makeText(
                this,
                "Wah gilak hpmu, coba kau tengok dulu ada gak folder /files di app.android.werdna.menari",
                Toast.LENGTH_LONG
            ).show()
        }

        trainImageList = listImages(File("$path${File.separator}train"))
        realImageList = listImages(File("$path${File.separator}real"))

        if (index >= trainImageList.size) {
            setIndex(-1)
            index = -1
        }

        Toast.makeText(this, "JANGAN KAU PENCET DULU LODING DIA NINUNINU", Toast.LENGTH_LONG).show()

        if (index == -1) {
            realImageList.forEach { _ ->
                inference(0, false, i)
                i++
            }

            nextImage()
        } else {
            huahua.setImageBitmap(decodeToBitmap(trainImageList[index]))
            asli.setImageBitmap(decodeToBitmap(realImageList[index]))
        }

        if (!pre) Toast.makeText(this, "HA PENCET LA KAU", Toast.LENGTH_LONG).show()
    }

    private fun getCurrentIndex(): Int {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        return sharedPref.getInt("index", -1)
    }

    private fun setIndex(a: Int) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putInt("index", a)
            apply()
        }
    }

    private fun updateIndex() {
        setIndex(index)
    }

    private fun resetIndex() {
        setIndex(-1)
        index = -1
        init(false)
    }

    private fun inference(label: Int, train: Boolean, idx: Int?) {
        poseDetector.process(InputImage.fromBitmap(decodeToBitmap(if (train) trainImageList[index] else realImageList[i]), 0))
            .addOnSuccessListener { results ->
                val data = mutableListOf<PointF?>()
                data.add(results.getPoseLandmark(PoseLandmark.LEFT_ANKLE)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.LEFT_HEEL)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.RIGHT_HEEL)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.LEFT_WRIST)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.RIGHT_WRIST)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.LEFT_PINKY)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.RIGHT_PINKY)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.LEFT_INDEX)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.RIGHT_INDEX)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.LEFT_THUMB)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.RIGHT_THUMB)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)?.position)
                data.add(results.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)?.position)

                appendToCSV(data, label, if (train) "train.csv" else "real.csv", if (train) index else idx!!)
                if (train) nextImage()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Wadoh error gan mampus kau",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun appendToCSV(data: List<PointF?>, label: Int, filename: String, idx: Int) {
        val filePath = "$path${File.separator}$filename"
        if (!File(filePath).exists()) {
            val writer = FileWriter(filePath, true)
            writer.append(HEADERS)
            writer.flush()
            writer.close()
        }
        val writer = FileWriter(filePath, true)
        val datum = data.joinToString(",", "\n$idx,", ",$label") { position ->
            "${position?.x},${position?.y}"
        }
        writer.append(datum)
        writer.flush()
        writer.close()
    }

    private fun listImages(root: File): List<File> {
        val listAllFiles = root.listFiles()
        return listAllFiles?.filter { file ->
            isImage(file)
        } ?: listOf()
    }

    private fun isImage(file: File): Boolean {
        val exts = listOf(".jpeg", ".jpg", ".png");
        exts.forEach { ext ->
            if (file.name.endsWith(ext)) return true
        }
        return false
    }

    private fun decodeToBitmap(file: File): Bitmap {
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    private fun nextImage() {
        if (index == trainImageList.size - 1) {
            Toast.makeText(
                this,
                "Sudah Siap!",
                Toast.LENGTH_LONG
            ).show()
        } else {
            huahua.setImageBitmap(decodeToBitmap(trainImageList[++index]))
            asli.setImageBitmap(decodeToBitmap(realImageList[index]))
            updateIndex()
        }
    }

    companion object {
        final val HEADERS = "id,left_ankle_x,left_ankle_y,right_ankle_x,right_ankle_y,left_heel_x,left_heel_y,right_heel_x,right_heel_y,left_foot_index_x,left_foot_index_y,right_foot_index_x,right_foot_index_y,left_wrist_x,left_wrist_y,right_wrist_x,right_wrist_y,left_pinky_x,left_pinky_y,right_pinky_x,right_pinky_y,left_index_x,left_index_y,right_index_x,right_index_y,left_thumb_x,left_thumb_y,right_thumb_x,right_thumb_y,left_shoulder_x,left_shoulder_y,right_shoulder_x,right_shoulder_y,label"
    }
}