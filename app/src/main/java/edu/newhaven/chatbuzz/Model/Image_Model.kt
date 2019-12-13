package edu.newhaven.chatbuzz.Model

class Image_Model(val id:String, val fromId: String?, val url:String?, val toId:String, val timeStamp:Long ){
        constructor() : this("","","","",-1)
}
