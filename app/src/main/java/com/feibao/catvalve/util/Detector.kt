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
    onFailure: ((e: Exception) -> Unit)? = null,
    onRes: (List<ImageLabel>) -> Unit
) {
    imgLabeler.process(InputImage.fromMediaImage(this, 0))
        .addOnSuccessListener {
            onRes.invoke(it)
        }
        .addOnFailureListener {
            onFailure?.invoke(it)
        }
}