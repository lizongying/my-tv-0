package com.lizongying.mytv0.data

import java.io.Serializable

data class Program(
    var id: Int = 0,
    var title: String = "",
    var description: String? = null,
    var logo: String = "",
    var image: String? = null,
) : Serializable {

    override fun toString(): String {
        return "Program{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", logo='" + logo + '\'' +
                ", image='" + image + '\'' +
                '}'
    }
}