package edu.newhaven.chatbuzz.Model

class Chat_Model(val id:String,val fromId: String, val text:String?, val toId:String, val timeStamp:Long ){
    constructor() : this("","","","",-1)
}
