package dev.luischang.semana05uesanpersmission

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import dev.luischang.semana05uesanpersmission.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

//import kotlin.jvm.Throws

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private var rutafotoActual=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTomarFoto.setOnClickListener {
            if (permisoEscrituraAlmacenamiento())
                llamarAppCamara()
            else
                solicitarPermisoSD()
        }

        binding.btnCompartir.setOnClickListener {
            if (rutafotoActual != ""){
                val contenidoUrl = FileProvider.getUriForFile(
                    applicationContext,
                    "dev.luischang.semana05uesanpersmission.provider",
                    File(rutafotoActual)
                )
                val enviarIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, contenidoUrl)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    type = "image/jpeg"
                }
                val eleccionIntent =
                    Intent.createChooser(enviarIntent, "Compartir Imagen")
                if(enviarIntent.resolveActivity(packageManager) != null){
                    startActivity(eleccionIntent)
                }
            }
        }

    }

    //Fun allowed to access the camera
    fun solicitarPermisoSD(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }
    //Fun allowed to access the camera
    fun llamarAppCamara(){
        val intent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        //try catch intent resolve
        if(intent.resolveActivity(packageManager) != null){
            //Create a temporal file
                val archivoImagen = crearArchivoTemporal()
            if(archivoImagen != null){
               val urlFoto: Uri =FileProvider.getUriForFile(
                   this,
                   "dev.luischang.semana05uesanpersmission.provider",
                   archivoImagen
               )
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, urlFoto)
                startActivityForResult(intent, 1)
            }
        }


    }

    @Throws(IOException::class)
    fun crearArchivoTemporal() : File? {
        val fechaHora = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val nombreImagen = "JPEG_${fechaHora}_"
        val directorio: File = this?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val imagen: File = File.createTempFile(nombreImagen,".jpg", directorio)
        rutafotoActual = imagen.absolutePath
        return imagen
    }

    fun permisoEscrituraAlmacenamiento():Boolean{
        val permisoEscritura = ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return permisoEscritura == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    //Function send message with Toast
    fun mostrarMensaje(mensaje:String){
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    //onRequestPermissionsResult
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            0 -> {
                if(grantResults.size>0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED){
                    llamarAppCamara()
                }else{
                    mostrarMensaje("Permiso denegado, no se puede acceder a la camara")
                }
            }
        }
    }

    fun mostrarFoto(){
        val exitInterface = ExifInterface(rutafotoActual)
        val orientacion: Int = exitInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        if(orientacion == ExifInterface.ORIENTATION_ROTATE_90){
            binding.imgFoto.rotation = 90.0F
        }else{
            binding.imgFoto.rotation = 0.0F
        }
        //Obteniendo la MetaData de la imagen.
        //val fecha = exitInterface.getAttribute(ExifInterface.TAG_DATETIME)
        val anchoImageView = binding.imgFoto.width
        val altoImageView = binding.imgFoto.height
        val bmOpciones = BitmapFactory.Options()
        bmOpciones.inJustDecodeBounds = true
        BitmapFactory.decodeFile(rutafotoActual, bmOpciones)
        val anchoFoto = bmOpciones.outWidth
        val altoFoto = bmOpciones.outHeight
        //Validate anchoImageView division by zero
        val factorReduccion = if (anchoImageView == 0) 1 else anchoFoto / anchoImageView
        val factorReduccionAlto = if (altoImageView == 0) 1 else altoFoto / altoImageView
        val escalaFoto = min(factorReduccion, factorReduccionAlto)

        //val escalaFoto = min(anchoFoto / anchoImageView, altoFoto / altoImageView)
        bmOpciones.inSampleSize = escalaFoto
        bmOpciones.inJustDecodeBounds = false
        val bitMapFoto = BitmapFactory.decodeFile(rutafotoActual, bmOpciones)
        binding.imgFoto.setImageBitmap(bitMapFoto)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                grabarFotoGaleria()
                mostrarFoto()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun grabarFotoGaleria(){
        val archivoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val nuevoArchivo = File(rutafotoActual)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            val contenidoUrl = FileProvider.getUriForFile(
                applicationContext,
                "dev.luischang.semana05uesanpersmission.provider",
                nuevoArchivo
            )
            archivoIntent.data = contenidoUrl
        }else{
            val contenidoUrl = Uri.fromFile(nuevoArchivo)
            archivoIntent.data = contenidoUrl
        }
        this.sendBroadcast(archivoIntent)
    }
}