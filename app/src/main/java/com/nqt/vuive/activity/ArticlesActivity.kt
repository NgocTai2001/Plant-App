package com.nqt.vuive.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nqt.vuive.R
import com.nqt.vuive.adapter.ArticlesAdapter
import com.nqt.vuive.databinding.ActivityArticlesBinding
import com.nqt.vuive.fragment.HomeFragment
import com.nqt.vuive.fragment.ProfileFragment
import com.nqt.vuive.model.Articles
import java.util.*
import kotlin.collections.ArrayList
import androidx.appcompat.widget.SearchView.OnQueryTextListener


class ArticlesActivity : AppCompatActivity(), View.OnClickListener {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var binding: ActivityArticlesBinding
    private var listArticles: ArrayList<Articles> = ArrayList()
    private lateinit var articlesAdapter: ArticlesAdapter
    private var filteredList: ArrayList<Articles> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticlesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        searchArticles()

        binding.revAticles.layoutManager = LinearLayoutManager(this)
        recyclesArticles()



        //Xử lý sự kiện khi nhấn back
        binding.btnBack.setOnClickListener(this@ArticlesActivity)

        // Xử lý sự kiện khi nhấn nút +
        binding.btnOpenCamera.setOnClickListener(this@ArticlesActivity)

        // Xử lý sự kiện khi nhấn nút trên BottomNavigationView
        binding.bottomNavigation.setOnItemSelectedListener {
            // Ẩn revAticles tránh trường hợp xuất hiện recycles
            binding.revAticles.visibility = View.GONE

            when(it.itemId){
                R.id.action_home -> replaceFragment(HomeFragment())
                R.id.action_profile -> replaceFragment(ProfileFragment())
                else ->{
                }
            }
            true
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_back -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

        }
    }

    override fun onBackPressed() {
        // Không làm gì khi nút back được nhấn
        // Bằng cách này, nút back sẽ không có hiệu ứng gì trong hoạt động này
    }

    private fun recyclesArticles() {

        val id = mAuth.currentUser?.uid
        Log.i("TAG_I", "loadData: id = $id")
        id?.let {
            database.collection("articles").get()
                .addOnSuccessListener {

                    //Rea data từ firestore, add vào listArticles
                    for (document in it) {
                        val articles = Articles()
                        articles.image_articles = document.getString("image_articles") ?: ""
                        articles.title = document.getString("title") ?: ""
                        articles.avatar_author = document.getString("avatar_author") ?: ""
                        articles.name = document.getString("name") ?: ""
                        articles.day = document.getString("day") ?: ""
                        articles.tag_articles = document.getString("tag_articles") ?: ""
                        articles.description_articles = document.getString("description_articles") ?: ""

                        listArticles.add(articles)
                    }

                    // Hiển thị adapter
                    articlesAdapter = ArticlesAdapter(listArticles, this@ArticlesActivity)
                    binding.revAticles.adapter = articlesAdapter

                    itemClick(listArticles)
                }
                .addOnFailureListener {
                }
        }
    }

    //Hàm xử lý khi click vào item bất kỳ
    private fun itemClick(inputList: kotlin.collections.ArrayList<Articles>){

        articlesAdapter.setOnItemClickListener(object : ArticlesAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {

                val intent = Intent(this@ArticlesActivity, DetailArticlesActivity::class.java)

                //Đính kèm data từ activity này sang activity khác
                intent.putExtra("image_articles", inputList[position].image_articles)
                intent.putExtra("title", inputList[position].title)
                intent.putExtra("avatar_author", inputList[position].avatar_author)
                intent.putExtra("name", inputList[position].name)
                intent.putExtra("day", inputList[position].day)
                intent.putExtra("tag_articles", inputList[position].tag_articles)
                intent.putExtra("description_articles", inputList[position].description_articles)



                //Chuyển sang activity mới và kết thúc activity hiện tại
                startActivity(intent)
                finish()
            }
        })
    }

    private fun searchArticles() {
        binding.searchViewArticles.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterArticles(query)
                performSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterArticles(newText)
                return true
            }
        })
    }

    //Hàm xử lý trong quá trình tìm kiếm
    private fun filterArticles(query: String) {
        filteredList.clear()

        if (query.isNotEmpty()) {
            val searchQuery = query.lowercase(Locale.getDefault())

            for (article in listArticles) {
                if (article.title.lowercase(Locale.getDefault()).contains(searchQuery) ||
                    article.name.lowercase(Locale.getDefault()).contains(searchQuery)
                ) {
                    filteredList.add(article)
                }
            }
        } else {
            filteredList.addAll(listArticles)
        }
        articlesAdapter = ArticlesAdapter(filteredList, this@ArticlesActivity)
        binding.revAticles.adapter = articlesAdapter
        itemClick(filteredList)
    }

    //Ẩn bàn phím khi nhấn Enter
    private fun performSearch(query: String) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.searchViewArticles.windowToken, 0)
    }

}