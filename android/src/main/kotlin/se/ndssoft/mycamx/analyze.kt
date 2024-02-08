package se.ndssoft.mycamx

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.Detection
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifierResult
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import java.nio.ByteBuffer

fun analyzeProxy(
    imageProxy: ImageProxy,
    objectDetector: ObjectDetector,
    imageClassifier: ImageClassifier,
): Map<String, Any> {
    //  Log.d("Analyze", "****************analyzeProxy started")
    var map = mapOf(
        "left" to 0f,
        "top" to 0f,
        "right" to 10f,
        "bottom" to 10f,
        "result" to "No dog in sight!",
    )
    // return map
    // Make mpImage from imageProxy
    val bitmapBuffer = Bitmap.createBitmap(
        imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
    )
    val byteBuffer = imageProxy.planes[0].buffer
    byteBuffer.rewind();
    bitmapBuffer.copyPixelsFromBuffer(byteBuffer)
    val mpImage = BitmapImageBuilder(bitmapBuffer).build()

    // Run Object Detector on mpImage
    val detectionResult = objectDetector.detect(mpImage)
    var det: Detection? = null
    if (detectionResult != null) {
        if (detectionResult!!.detections().size > 0) {

            // If more than one dog, select the dog with the biggest bounding box
            var rectMax = 0.0f
            var saveIndex = 0
            for (i in 0 until detectionResult!!.detections().size) {
                det = detectionResult!!.detections()[i]
                val left = det.boundingBox().left
                val top = det.boundingBox().top
                val right = det.boundingBox().right
                val bottom = det.boundingBox().bottom
                var rectSize = (right - left) * (bottom - top)
                if (rectSize > rectMax) {
                    rectMax = rectSize
                    saveIndex = i
                }
            }
            det = detectionResult!!.detections()[saveIndex]


            // Crop the image: Create a new mpImage with what is inside the bounding box
            val l = det.boundingBox().left.toInt()
            val t = det.boundingBox().top.toInt()
            val w = det.boundingBox().width().toInt()
            val h = det.boundingBox().height().toInt()
            val size = w * h * 4
            val smallBuffer = ByteBuffer.allocateDirect(size)
            try {
                val wtot = imageProxy.width
                smallBuffer.rewind()
                byteBuffer.rewind()
                var pixel = ByteArray(4)
                for (rowNumber in 0..h - 1) {
                    for (pixelNumber in 0..w - 1) {
                        val offset = (rowNumber + t) * wtot * 4 + (l + pixelNumber) * 4
                        pixel[0] =
                            byteBuffer[offset] // index out of range happens here
                        pixel[1] = byteBuffer[offset + 1]
                        pixel[2] = byteBuffer[offset + 2]
                        pixel[3] = byteBuffer[offset + 3]
                        smallBuffer.put(pixel)
                    }
                }

            } catch (exc: Exception) {
                Log.e("Analyze", "Crop failed", exc)
            }

            // Convert smallBuffer to mpImage
            smallBuffer.rewind()
            val bitmapBuffer2 = Bitmap.createBitmap(
                w, h, Bitmap.Config.ARGB_8888
            )
            bitmapBuffer2.copyPixelsFromBuffer(smallBuffer)
            val mpImage2 = BitmapImageBuilder(bitmapBuffer2).build()

            // Run Image Classifier with cropped image as input
            val classifierResult: ImageClassifierResult? =
                imageClassifier.classify(mpImage2)
            //   Log.d("Analyze", "****************got classifyer  result")

            if (classifierResult!!.classificationResult()
                    .classifications().size > 0
            ) {
                // Create a string with one or two breeds and their percentages
                var det2 =
                    classifierResult!!.classificationResult().classifications()[0]
                if (det2.categories().size > 0) {
                    var category = det2.categories()[0]
                    var result =
                        category.categoryName().substring(10).replaceFirstChar { it.uppercase() } + " " + String.format(
                            "%.0f",
                            category.score() * 100
                        ) + " %"
                    if (det2.categories().size > 1) {
                        category = det2.categories()[1]
                        val result1 =
                            category.categoryName().substring(10).replaceFirstChar { it.uppercase() } + " " + String.format(
                                "%.0f",
                                category.score() * 100
                            ) + " %"
                        result = result + "\n" + result1
                    }
                    // Return map to be sent to Flutter
                    map = mapOf(
                        "left" to det.boundingBox().left,
                        "top" to det.boundingBox().top,
                        "right" to det.boundingBox().right,
                        "bottom" to det.boundingBox().bottom,
                        "result" to result,
                    )
                }
            }
        }
    }
    //  Log.d("Analyze", "****************analyzeProxy finished")
    return map
}



