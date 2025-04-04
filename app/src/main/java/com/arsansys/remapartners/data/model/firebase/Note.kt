package com.arsansys.remapartners.data.model.firebase

class Note {
    internal var subject: String? = null
    internal var content: String? = null
    internal var token: String? = null
    internal var imageUrl: String? = null
    internal var data: MutableMap<String?, String?>? = null
}