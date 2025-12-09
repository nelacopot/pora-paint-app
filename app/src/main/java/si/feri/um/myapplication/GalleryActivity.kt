package si.feri.um.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class GalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val tvNoImages = findViewById<TextView>(R.id.tv_no_images)

        //seznam URI-jev slik
        val imageUris = getAllImages()

        if (imageUris.isEmpty()) {
            tvNoImages.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvNoImages.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            //nastavljanje RecyclerView
            recyclerView.layoutManager = GridLayoutManager(this, 3) // 3 slike v vrsto
            recyclerView.adapter = ImageAdapter(imageUris) { uri ->
                //ob kliku odpri sliko v sistemskem pregledovalniku
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "image/*")
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                startActivity(intent)
            }
        }
    }

    private fun getAllImages(): List<Uri> {
        val imageList = mutableListOf<Uri>()

        //pristop za Android 10+ (Q) preko MediaStore
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val projection = arrayOf(MediaStore.Images.Media._ID)
            //slike v mapi MojeRisbe
            val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf("%Pictures/MojeRisbe%")
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                    imageList.add(contentUri)
                }
            }
        } else {
            //za starejse verzije
            val imagesDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MojeRisbe")
            if (imagesDir.exists()) {
                val files = imagesDir.listFiles()
                files?.filter { it.extension == "jpg" || it.extension == "png" }
                    ?.sortedByDescending { it.lastModified() }
                    ?.forEach { file ->
                        imageList.add(Uri.fromFile(file))
                    }
            }
        }
        return imageList
    }
}