package com.maxigimenez.nodes.core.util

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.maxigimenez.nodes.R

object ToastHelper {

    /**
     * Muestra un Toast con vista personalizada.
     * En Android 12+ (API 31), el sistema restringe vistas personalizadas y aplica formato estándar.
     */
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        try {
            val inflater = LayoutInflater.from(context)
            val layout = inflater.inflate(R.layout.custom_toast, null)

            val icon = layout.findViewById<ImageView>(R.id.toast_icon)
            icon.setImageResource(R.mipmap.ic_launcher)

            val text = layout.findViewById<TextView>(R.id.toast_text)
            text.text = message

            val toast = Toast(context)
            toast.duration = duration
            toast.view = layout
            toast.show()
        } catch (e: Exception) {
            // Fallback al Toast del sistema ante excepciones en la inflación de vista personalizada
            Toast.makeText(context, message, duration).show()
        }
    }
}
