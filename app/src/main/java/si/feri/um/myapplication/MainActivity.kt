package si.feri.um.myapplication

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.material.slider.RangeSlider
import com.mihir.drawingcanvas.drawingView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import androidx.core.graphics.createBitmap

class MainActivity : AppCompatActivity() {

    private lateinit var drawView: drawingView
    private lateinit var rangeSlider: RangeSlider
    private lateinit var btnColorPicker: ImageButton

    private var currentBrushColor: Int = Color.BLACK

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        drawView = findViewById(R.id.draw_view)
        rangeSlider = findViewById(R.id.rangebar)

        val btnUndo = findViewById<ImageButton>(R.id.btn_undo)
        val btnRedo = findViewById<ImageButton>(R.id.btn_redo)
        val btnBrushSize = findViewById<ImageButton>(R.id.btn_brush_size)
        val btnClear = findViewById<ImageButton>(R.id.btn_clear)
        val btnSave = findViewById<ImageButton>(R.id.btn_save)
        val btnGallery = findViewById<ImageButton>(R.id.btn_gallery)
        btnColorPicker = findViewById(R.id.btn_color_picker)

        // UNDO
        btnUndo.setOnClickListener { drawView.undo() }

        // REDO
        btnRedo.setOnClickListener { drawView.redo() }

        // CLEAR
        btnClear.setOnClickListener { drawView.clearDrawingBoard() }

        // BRUSH SIZE PANEL
        btnBrushSize.setOnClickListener {
            rangeSlider.visibility = if (rangeSlider.isVisible) View.GONE else View.VISIBLE
        }

        // Default brush size
        drawView.setSizeForBrush(10)

        rangeSlider.addOnChangeListener { _, value, _ ->
            drawView.setSizeForBrush(value.toInt())
        }

        // COLOR PICKER (Skydoves)
        btnColorPicker.setOnClickListener {
            ColorPickerDialog.Builder(this)
                .setTitle("Choose a color")
                .setPreferenceName("MyColorPicker")
                .setPositiveButton("OK",
                    ColorEnvelopeListener { envelope, _ ->
                        val pickedColor = envelope.color
                        currentBrushColor = pickedColor
                        drawView.setBrushColor(pickedColor)
                        btnColorPicker.setColorFilter(pickedColor)
                        btnColorPicker.setBackgroundColor(pickedColor)
                    }
                )
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        // SAVE
        btnSave.setOnClickListener {
            val bitmap = getBitmapFromView(drawView)
            saveImageToStorage(bitmap)
        }

        // GALLERY
        btnGallery.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }
    }

    private fun saveImageToStorage(bitmap: Bitmap) {
        val filename = "Risanje_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream?

        try {
            fos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MojeRisbe")
                }

                val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                contentResolver.openOutputStream(imageUri!!)
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val imageFile = File(imagesDir, filename)
                FileOutputStream(imageFile)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

            Toast.makeText(this, "Slika uspešno shranjena!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Napaka: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = createBitmap(view.width, view.height)
        val canvas = android.graphics.Canvas(returnedBitmap)

        view.background?.draw(canvas) ?: canvas.drawColor(Color.WHITE)
        view.draw(canvas)

        return returnedBitmap
    }
}
