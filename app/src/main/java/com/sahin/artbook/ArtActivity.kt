package com.sahin.artbook

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.sahin.artbook.databinding.ActivityArtBinding
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class ArtActivity : AppCompatActivity() {
    private lateinit var binding : ActivityArtBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionResultLauncher: ActivityResultLauncher<String>
    private lateinit var myDatabase : SQLiteDatabase
    var selectedBitmap : Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myDatabase = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

        registerLauncher()

        val intent = intent
        val info = intent.getStringExtra("info")

        if(info.equals("new")){
            binding.artNameText.setText("")
            binding.artistNameText.setText("")
            binding.yearText.setText("")
            binding.imageView.setImageResource(R.drawable.select)
            binding.saveButton.visibility = View.VISIBLE

        }else{
            binding.saveButton.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",1)

            val cursor = myDatabase.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))


            val artNameIx = cursor.getColumnIndex("artname")
            val artistNameIx = cursor.getColumnIndex("artistname")
            val yearIx = cursor.getColumnIndex("year")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.artNameText.setText(cursor.getString(artNameIx))
                binding.artistNameText.setText(cursor.getString(artistNameIx))
                binding.yearText.setText(cursor.getString(yearIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)

            }
            cursor.close()


        }
    }
    fun selectImage(view : View){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            // Android 33+ READ_MEDİA_İMAGES
            // İzinleri kontrol et izin verilmemiş ise izin almaya çalış.
            if(ContextCompat.checkSelfPermission(this@ArtActivity,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this@ArtActivity,Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this@ArtActivity,Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED){

                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    // rationale -> mantıksal izin iste
                    Snackbar.make((view),"Permission needed for galery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                        // request permission -> izin iste
                        permissionResultLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                }else{
                    // request permission -> izin iste
                    permissionResultLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                }

            }else{
                // izin verildiyde ne yapılacağı yazılır.
                // intent ile galeriye git ve bir seçim yap denildi.
                val intentToGalery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                // intent
                activityResultLauncher.launch(intentToGalery)
            }
        }else{
            // Android 33- READ_EXTERNAL_STORAGE
            // İzinleri kontrol et izin verilmemiş ise izin almaya çalış.
            if(ContextCompat.checkSelfPermission(this@ArtActivity,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ){

                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    // rationale -> mantıksal izin iste
                    Snackbar.make((view),"Permission needed for galery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                        // request permission -> izin iste
                        permissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
                }else{
                    // request permission -> izin iste
                    permissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                }

            }else{
                // izin verildiyde ne yapılacağı yazılır.
                // intent ile galeriye git ve bir seçim yap denildi.
                val intentToGalery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                // intent
                activityResultLauncher.launch(intentToGalery)
            }
        }


    }
    fun save(view : View){
        val artName = binding.artNameText.text.toString()
        val artistName = binding.artistNameText.text.toString()
        val year = binding.yearText.text.toString()
        if(selectedBitmap != null){
            // görselli png,jpg formatında kaydedemeyiz veri tabanına
            val smallBitmap = makeSmaller(selectedBitmap!!,300)

            // Görselli byte arrayine dönüştür.
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            // Veriler alındı veritabanına kaydedebilirsin.
            try {
                 //myDatabase = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

                // Eğer elimizde bytearray dizisi var ise blob olarak kaydedebiliriz.
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY,artname VARCHAR,artistname VARCHAR,year VARCHAR,image BLOB) ")

                val sqlString = "INSERT INTO arts(artname,artistname,year,image) VALUES (?,?,?,?)"
                // statement verileri bağlamak için kullanıyoruz.
                val statement = myDatabase.compileStatement(sqlString)

                // Verilerin bağlanması
                statement.bindString(1,artName)
                statement.bindString(2,artistName)
                statement.bindString(3,year)
                statement.bindBlob(4,byteArray)

                statement.execute()



            }catch (e : Exception){
                e.printStackTrace()
            }

            val intent = Intent(this@ArtActivity,MainActivity::class.java)
            // Bütün activityleri kapat demektir.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

    }
    // Bitmap küçültmek için kullandığımız fonksiyon.
    private fun makeSmaller(image : Bitmap,maximumSize : Int) : Bitmap{
        var height= image.height
        var width = image.width


        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if(bitmapRatio > 1){
            // Landscape -> Yatay
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()

        }else{
            // Portrait -> Dikey
            height = maximumSize
            val scaledWidht = height * bitmapRatio
            width = scaledWidht.toInt()

        }
        // Ya küçült ya da büyült demektir.
        return Bitmap.createScaledBitmap(image,width,height,true)
    }
    private fun registerLauncher(){
        // galeriye gidildi sonucu verildi.
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            // galeriye düzgün gidildi mi ?
            if(result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                    val imageData = intentFromResult.data
                    // SQLite veritabanına kayıt için bitmap olarak alıp görselli küçültüp bytearray olarak kaydetmem lazım.
                    //binding.imageView.setImageURI(imageData)
                    if (imageData != null){
                        try {
                            // verileri görselle çevirir.
                            if(Build.VERSION.SDK_INT >= 28){
                                val source = ImageDecoder.createSource(this@ArtActivity.contentResolver,imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }else{
                                selectedBitmap = MediaStore.Images.Media.getBitmap(this@ArtActivity.contentResolver,imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)

                            }




                        }catch (e : Exception){
                            e.printStackTrace()
                        }
                    }

                }

            }

        }
        permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            // eğer doğruysa izin verildi demektir.
            if(result ){
                // permission granted
                val intentToGalery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                // intent
                activityResultLauncher.launch(intentToGalery)

            }else{
                // permission denied
                Toast.makeText(this@ArtActivity,"Give Permission!!",Toast.LENGTH_SHORT).show()
            }

        }

    }
}