package dev.luischang.semana05uesanpersmission

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import dev.luischang.semana05uesanpersmission.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
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
}