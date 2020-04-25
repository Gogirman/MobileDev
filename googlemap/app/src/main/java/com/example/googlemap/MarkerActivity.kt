package com.example.googlemap

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
import android.widget.ImageView.ScaleType
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.gms.maps.model.LatLng
import io.realm.Realm
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MarkerActivity: AppCompatActivity() {
    private val REQUEST_TAKE_PHOTO = 1
    private val REQUEST_IMAGE_CAPTURE = 1

    lateinit var title:TextView
    lateinit var button:Button
    lateinit var sv:LinearLayout

    lateinit var mCurrentPhotoPath: String
    lateinit var db: Realm
    lateinit var position: LatLng


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marker)
        title = findViewById(R.id.textTitle)
        button = findViewById(R.id.makePhotoButton)
        sv = findViewById(R.id.picturesView)

        position = intent.getParcelableExtra("pos")

        Realm.init(this)
        db = Realm.getDefaultInstance()

        for(picturePath in db.where(MarkerModel::class.java).
            equalTo("latitude", position.latitude).
            equalTo("longitude", position.longitude)
            .findFirst()?.pictures!!)
        {
            addPicture(picturePath)
        }

        title.text = "Location: (${position.latitude},${position.longitude})"

    }


    private fun addPicture(picturePath: String)
    {
        var imageView = ImageView(sv.getContext())
        sv.addView(imageView)

        imageView.adjustViewBounds = true
        imageView.visibility = View.VISIBLE
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        params.setMargins(16, 16, 16, 16)
        imageView.scaleType = ScaleType.FIT_CENTER
        imageView.layoutParams = params

        // Get the dimensions of the View
        var displayMetrics: DisplayMetrics = DisplayMetrics();
        windowManager.defaultDisplay.getMetrics(displayMetrics);
        imageView.maxWidth = displayMetrics.widthPixels / 3
        imageView.maxHeight = displayMetrics.heightPixels / 3


        if(Build.VERSION.SDK_INT < 28) {
            val bitmap = MediaStore.Images.Media.getBitmap(
                this.contentResolver,
                Uri.parse(picturePath)
            )
            imageView.setImageBitmap(bitmap)
        } else {
            val source = ImageDecoder.createSource(this.contentResolver, Uri.parse(picturePath))
            val bitmap = ImageDecoder.decodeBitmap(source)
            imageView.setImageBitmap(bitmap)
        }
    }

    fun makePhotoClick(view: View)
    {
        val takePictureIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (e: IOException) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }

            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(this,
                "com.example.photomap.android.fileprovider",
                photoFile);
                /*takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(photoFile))*/
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    private fun createImageFile(): File? { // создание файла с уникальным именем
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File? = getExternalFilesDir(DIRECTORY_PICTURES)
        val image: File = File.createTempFile(
            imageFileName,  /* префикс */
            ".jpg",  /* расширение */
            storageDir /* директория */
        )
        // сохраняем пусть для использования с интентом ACTION_VIEW
        mCurrentPhotoPath = "file:" + image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            addPicture(mCurrentPhotoPath)

            val mapMarker: MarkerModel? = db.where(MarkerModel::class.java)
                .equalTo("latitude", position.latitude)
                .equalTo("longitude", position.longitude)
                .findFirst()
            if (mapMarker != null) {
                db.beginTransaction()
                mapMarker.pictures.add(mCurrentPhotoPath)
                db.commitTransaction()
            }
        }
    }
}