package com.example.vehicleocr

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.net.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var numberImg: ImageView
    lateinit var detailsTxt: TextView
    lateinit var ownerData: TextView
    lateinit var mCurrentPhotoUri: Uri
    private val GALLERY_REQUEST_CODE = 0
    private val TAKE_PHOTO_REQUEST_CODE = 1

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        numberImg = findViewById(R.id.numberImg)
        detailsTxt = findViewById(R.id.vDetails)
        ownerData = findViewById(R.id.ownerDetails)

    }

//    fun selectImage(v: View) {
//        val intent = Intent()
//        intent.type = "image/*"
//        intent.action = Intent.ACTION_GET_CONTENT
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    numberImg.setImageURI(data!!.data)
                    getVehicleDetails()
                }
                TAKE_PHOTO_REQUEST_CODE -> {
                    numberImg.setImageURI(mCurrentPhotoUri)
                    getVehicleDetails()
                }
                else -> {
                    Toast.makeText(this, "Select an Image First", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun getVehicleDetails() {
        if (numberImg.drawable != null) {
            detailsTxt.setText("Vehicle No.: ")
            val bitmap = (numberImg.drawable as BitmapDrawable).bitmap
            val image = FirebaseVisionImage.fromBitmap(bitmap)
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
            detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->
                    processResultText(firebaseVisionText)
                }
                .addOnFailureListener {
                    detailsTxt.setText("Failed")
                }
        } else {
            Toast.makeText(this, "Select an Image First", Toast.LENGTH_LONG).show()
        }

    }


    private fun processResultText(resultText: FirebaseVisionText) {
        if (resultText.textBlocks.size == 0) {
//            detailsTxt.setText("No Text Found")
            return
        }
//        for (block in resultText.textBlocks) {
//            val blockText = block.text
//            detailsTxt.append(blockText + "\n")
//        }
        val userData = "\nOwner Name :   Atul Singh\n\n" +
                "Owner’s Father Name :   Ajit Singh\n\n" + "Address :   Gandhinagar, Gujarat, Ahmedabad, 382016"
        val vehicleData = "Vehicle No. :   GJ01KV3713"
        val details = "$vehicleData\n$userData"
        val intent = Intent(applicationContext, DetailActivity::class.java)
        intent.putExtra("DATA", details)
        intent.putExtra("IMAGE", mCurrentPhotoUri.toString())
        startActivity(intent)
        Log.d("abhi", "number image text : ${detailsTxt.text}")

        Thread(Runnable {
            kotlin.run {
                getDetails("PB65AM0008")
            }
        }).start()

    }

    fun takePhoto(v: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Make sure that there is activity of the camera that processes the intent
        if (intent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (photoFile != null) {
                mCurrentPhotoUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    photoFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri)
                startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "ER_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }

//    fun getImage() {
//
//        val client = OkHttpClient()
//
//        var image =
//            "{\"url\": \"https://www.autocar.co.uk/sites/autocar.co.uk/files/styles/body-image/public/giulianumberplate.jpg\"}"
//        val mediaType = MediaType.parse("application/json")
//        val body = RequestBody.create(
//            mediaType, image
//        )
//        val request = Request.Builder()
//            .url("https://license-plate-detection.p.rapidapi.com/license-plate-detection")
//            .post(body)
//            .addHeader("content-type", "application/json")
//            .addHeader("X-RapidAPI-Key", "7b0623c2d8mshcc23c5637e2eb03p1be30ejsn621a4ad0b999")
//            .addHeader("X-RapidAPI-Host", "license-plate-detection.p.rapidapi.com")
//            .build()
//
//        val response = client.newCall(request).execute()
//        Log.d("abhi", "image text : ${response}")
//    }

    fun getDetails(vehicleNumber: String) {
        val client = OkHttpClient()

        val mediaType = MediaType.parse("application/json")

        var data = "{\"VehicleNumber\": \"$vehicleNumber\"}"


        val body = RequestBody.create(
            mediaType, data
        )
        val request = Request.Builder()
            .url("https://vehicle-rc-information.p.rapidapi.com/")
            .post(body)
            .addHeader("content-type", "application/json")
            .addHeader("X-RapidAPI-Key", "7b0623c2d8mshcc23c5637e2eb03p1be30ejsn621a4ad0b999")
            .addHeader("X-RapidAPI-Host", "vehicle-rc-information.p.rapidapi.com")
            .build()

        val response = client.newCall(request).execute()
        Log.d("abhi", "car details : ${data.toString()}:${response}")
    }


}