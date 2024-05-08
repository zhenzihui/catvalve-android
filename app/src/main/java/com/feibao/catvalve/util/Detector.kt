package com.feibao.catvalve.util

import android.media.Image
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

val imgLabeler by lazy {
    ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
}


fun Image.findLabels(
    onComplete: () -> Unit,
    onFailure: ((e: Exception) -> Unit)? = null,
    onRes: (List<ImageLabel>) -> Unit
) {
    imgLabeler.process(InputImage.fromMediaImage(this, 0))
        .addOnSuccessListener {
            onRes.invoke(it)
        }
        .addOnFailureListener {
            "find labels failed! $it".print()
            onFailure?.invoke(it)
        }
        .addOnCompleteListener {
            onComplete.invoke()
        }
}