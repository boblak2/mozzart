package com.example.koty

class Item {
    var name: String? = null
    var path: String? = null
}

class Food(private var name: String, private var type: Int, var  age: Int, riba: Int){

    constructor(burek: String) : this("", 33, 44, 55) {
        age = 33
    }

    fun njam(){

    }

}