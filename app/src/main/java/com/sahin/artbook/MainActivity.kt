package com.sahin.artbook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.sahin.artbook.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var artList : ArrayList<Art>
    private lateinit var artAdapter: ArtAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        artList = ArrayList<Art>()


        // Boş listedeki verileri adapterde gösterir.
        artAdapter = ArtAdapter(artList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = artAdapter


        try {
            val myDatabase = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

            val cursor = myDatabase.rawQuery("SELECT * FROM arts",null)

            val idIx = cursor.getColumnIndex("id")
            val artNameIx = cursor.getColumnIndex("artname")

            while (cursor.moveToNext()){
                val id = cursor.getInt(idIx)
                val name = cursor.getString(artNameIx)

                val art = Art(id,name)

                artList.add(art)








            }
            artAdapter.notifyDataSetChanged()
            cursor.close()



        }catch (e : Exception){
            e.printStackTrace()
        }

    }

    // Tıklandığında ne olacağını yazarız.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.add_art_item){
            val intent = Intent(this@MainActivity,ArtActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    // Bağlama işlemi yapılır.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // inflater
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.art_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}