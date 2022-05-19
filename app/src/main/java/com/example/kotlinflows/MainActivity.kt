package com.example.kotlinflows

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.kotlinflows.databinding.ActivityMainBinding
import androidx.databinding.DataBindingUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


class MainActivity : AppCompatActivity() {
    // for  data_Binding
    lateinit var binding : ActivityMainBinding
    private lateinit var fixedflow : Flow<Int>
    private lateinit var collectionflow : Flow<Int>
    private lateinit var channelflow : Flow<Int>
    private lateinit var lambdaflow : Flow<Int>
    private  lateinit var flowchain: Flow<Int>


    private val list = listOf(1,2,3,4,5)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setting up the Binding
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        // calling methods
        setupfixedflow()
        setupflowfromcollection()
        setupflowithlambda()
        setupchannelflowithlambda()
        playwithmapsync()
        playwithmapsuspend()
        playwithFiltersync()
        playwithFiltersuspend()
        playwithtake()
        playwithtakewhile()

        // onclick of fixed flow
        binding.fixed.setOnClickListener{
          collectfixedflow()

        }
        //onclick of collection flow
        binding.collection.setOnClickListener{
            collectCollectionflow()
        }

        binding.lambda.setOnClickListener{
            collectlambdaflow()
        }

        binding.channel.setOnClickListener{
            collectchannelflow()
        }

        binding.flowchain.setOnClickListener {
            runBlocking {
                createflowchain()
            }

            CoroutineScope(Dispatchers.Main).launch {
                createflowchain()
            }


        }
        binding.MapSync.setOnClickListener {
            playwithmapsync()
        }

        binding.Mapsuspend.setOnClickListener {
            playwithmapsuspend()
        }
        binding.Filtersync.setOnClickListener {
            playwithFiltersync()
        }
        binding.Filtersuspend.setOnClickListener {
            playwithFiltersuspend()
        }
        binding.take.setOnClickListener {
            playwithtake()
        }
        binding.takewhile.setOnClickListener {
            playwithtakewhile()
        }
        binding.zip.setOnClickListener {
            zipmethod()
        }
    }

    private fun zipmethod() {
      val numbrs = (1..3).asFlow()
        val strs =  flowOf("one","two","three")
        runBlocking {
            // Zip combines the two flows
            numbrs.zip(strs){ a,b -> "$a -> $b" }
                .collect { value -> print("$value") }
        }
    }

    private fun playwithtakewhile() {

        val start_time = System.currentTimeMillis()
        runBlocking {
            // flow of request
            (1..1000).asFlow().
                // take while flow operator
                // it will print 10 milisecond of starting
            takeWhile {System.currentTimeMillis() - start_time < 10  }
                .collect { response -> println(response) }
        }
    }

    private fun playwithtake() {
        runBlocking {
            // flow of request
            (1..10).asFlow().
                // it will take to no of count
            take (4)
                .collect { response -> println(response) }
        }

    }

    private fun playwithFiltersuspend() {
        runBlocking {
            // flow of request
            (1..10).asFlow().
                // filter will filter with the given condition
                // here we are filtering the odd numbers
            filter {num -> filteroddnumberssuspend(num) }
                .collect { response -> println(response) }
        }
    }

    private suspend  fun filteroddnumberssuspend(num: Int): Boolean {
       delay(300)
        return (num%2==0).not()
    }

    private fun playwithFiltersync() {
        runBlocking {
            // flow of request
            (1..10).asFlow().
            filter {num -> filteroddnumbers(num) }
                .collect { response -> println(response) }
        }
    }

    private fun filteroddnumbers(num: Int): Boolean {
         return (num%2==0).not()

    }

    private fun playwithmapsuspend() {

        runBlocking {
            // flow of request
            (1..3).asFlow().
                //map is the flow operator
            map {num -> performrequest(num) }
                .collect { response -> println(response) }
        }

    }

    private suspend fun performrequest(num: Int):String {

        delay(300)
        return "response in suspend $num"

    }

    // mapsync method
    private fun playwithmapsync() {

        runBlocking {
            // flow of request
            (1..3).asFlow().
                //map is the flow operator
            map {num -> performsyncoperation(num) }
                .collect { response -> println(response) }
        }
    }

    private fun performsyncoperation(num: Int): String {
      return "response in sync $num"

    }

    // maked this method as a suspend beacause of collect that should be should in suspend function or in coroutine scope
    private suspend fun createflowchain() {
        flow {
            (0..10).forEach{
                //delay means time delay
                delay(300)
                emit(it)
            }
        }.collect{
            Log.i(TAG,"from chain $it")
        }
    }

    private fun collectchannelflow() {
        CoroutineScope(Dispatchers.Main).launch {
            channelflow.collect { item ->
                Log.i(TAG,"$item")
            }
        }
    }

    private fun collectlambdaflow() {
        // MAIN is for main thread
        CoroutineScope(Dispatchers.Main).launch {
            lambdaflow.collect { item ->
                Log.i(TAG,"$item")
            }
        }
    }

    private fun setupchannelflowithlambda() {
        // it uses cahnnel to communicate to collector it is also cold.
    channelflow = channelFlow {
        (1..5).onEach{
            delay(300)
        send(it)}
    }
    }

    private fun setupflowithlambda() {
        //It is having flow collecter Implementation
        lambdaflow = flow {
            (1..5).onEach{
                delay(300)
                emit(it)}
        }

    }

    /**
     * collect is the suspend function so for that we had to put it in a coroutine scope
     */
    private fun collectCollectionflow() {

        CoroutineScope(Dispatchers.Main).launch {
            collectionflow.collect { item ->
                Log.i(TAG,"$item")
            }
        }
    }

    /**
     * whenever we call asflow with any list etc this will take a iterable and creates a flow out of it and gives you
     */
    private fun setupflowfromcollection() {
        collectionflow = list.asFlow().onEach { delay(300) }

    }


    private fun collectfixedflow() {
        //launching the coroutines by using launch and  implementing by using dispatchers.IO
      CoroutineScope(Dispatchers.Main).launch {
          fixedflow.collect { item ->
              Log.i(TAG,"$item")
          }
      }
    }

    /**
     *  whenever we need fixed flow means the element should be fixed and you dont what to change in future use flowof..
     */
    private fun setupfixedflow() {
        fixedflow = flowOf(1,2,3,4,5).onEach { delay(300) }

    }
}