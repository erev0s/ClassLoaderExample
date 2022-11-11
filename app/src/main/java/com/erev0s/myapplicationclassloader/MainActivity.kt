package com.erev0s.myapplicationclassloader

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import dalvik.system.DexClassLoader
import dalvik.system.InMemoryDexClassLoader
import dalvik.system.PathClassLoader
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File
import java.net.URL
import java.net.URLConnection
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    lateinit var tvDexRandomNumber: TextView
    lateinit var tvPathRandomNumber: TextView
    lateinit var tvinMemoryRandomNumber: TextView
    lateinit var buffer: ByteArray
    lateinit var btBuffer: ByteBuffer
    val dexFilename: String = "classes.dex"
    val pathFilename: String = "classes2.dex"
    lateinit var loader: DexClassLoader
    lateinit var pathLoader: PathClassLoader
    val inMemoryDownloadURL: String = "http://10.0.3.2:8989/classes3.dex"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // DexClassLoader
        loader = cl(dexFilename)
        val loadClass = loader.loadClass("com.erev0s.randomnumber.RandomNumber")
        val checkMethod = loadClass.getMethod("getRandomNumber")
        val cl_in = loadClass.newInstance()

        tvDexRandomNumber = findViewById(R.id.randomNumber)
        val gbutton = findViewById<Button>(R.id.gButton)
        gbutton.setOnClickListener {
            tvDexRandomNumber.text = checkMethod.invoke(cl_in) as String
        }



        // PathClassLoader
        pathLoader = path_cl(pathFilename)
        val loadClassPath = pathLoader.loadClass("com.erev0s.randomnumber.RandomNumber2")
        val checkMethodPath = loadClassPath.getMethod("getRandomNumber")
        val cl_in_path = loadClassPath.newInstance()

        tvPathRandomNumber = findViewById(R.id.randomNumberPath)
        val gButtonPath = findViewById<Button>(R.id.gButtonPath)
        gButtonPath.setOnClickListener {
            tvPathRandomNumber.text = checkMethodPath.invoke(cl_in_path) as String
        }



        // InMemoryDexClassLoader
        val dbutton = findViewById<Button>(R.id.dButton)
        dbutton.setOnClickListener {
            downloadFile(inMemoryDownloadURL)
        }
        tvinMemoryRandomNumber = findViewById(R.id.randomNumberInMemory)
        val gbuttoninMemory = findViewById<Button>(R.id.gButtonInMemory)
        gbuttoninMemory.setOnClickListener {
            if (!this::buffer.isInitialized) {
                Toast.makeText(baseContext, "Are you serving the dex file so you can download it?", Toast.LENGTH_LONG).show()
            } else {
                btBuffer = ByteBuffer.wrap(buffer)
                val lder = InMemoryDexClassLoader(btBuffer, this.javaClass.classLoader)
                val mt = lder.loadClass("com.erev0s.randomnumber.RandomNumber3")
                val checkMethodInMemory = mt.getMethod("getRandomNumber")
                val newcl = mt.newInstance()
                tvinMemoryRandomNumber.text = checkMethodInMemory.invoke(newcl)!!.toString()
            }
        }
    }

    private fun cl(dexFileName: String): DexClassLoader {
        // Create a dex dir to hold the DEX file to be loaded
        val dexFile: File = File.createTempFile("pref", ".dex")
        val inStr: ByteArrayInputStream =
            ByteArrayInputStream(baseContext.assets.open(dexFileName).readBytes())
        inStr.use { input ->
            dexFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        val loader: DexClassLoader = DexClassLoader(
            dexFile.absolutePath,
            null,
            null,
            this.javaClass.classLoader
        )
        return loader
    }

    private fun path_cl(filename: String): PathClassLoader {
        // Create a dex dir to hold the DEX file to be loaded
        val dexFile: File = File.createTempFile("pref", ".dex")
        val inStr: ByteArrayInputStream =
            ByteArrayInputStream(baseContext.assets.open(filename).readBytes())
        inStr.use { input ->
            dexFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        val loader: PathClassLoader = PathClassLoader(dexFile.absolutePath, this.javaClass.classLoader)
        return loader
    }

    private fun downloadFile(url: String) {
        val thread = Thread {
            try {
                val u = URL(url)
                val conn: URLConnection = u.openConnection()
                val contentLength: Int = conn.getContentLength()
                val stream = DataInputStream(u.openStream())
                buffer = ByteArray(contentLength)
                stream.readFully(buffer)
                stream.close()
                Log.d("seccheck", "Success of download to buffer")
            } catch (e: Exception) {
                Log.e("seccheck", e.message!!)
            }
        }
        thread.start()
    }
}