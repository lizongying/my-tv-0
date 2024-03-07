package com.lizongying.mytv0.models

import java.io.Serializable

data class TV(
    var id: Int = 0,
    var programId: String = "",
    var title: String = "",
    var description: String? = null,
    var logo: String = "",
    var image: String? = null,
    var videoUrl: List<String>,
    var headers: Map<String, String>? = null,
    var category: String = "",
    var child: List<TV>,
) : Serializable {

    override fun toString(): String {
        return "TV{" +
                "id=" + id +
                ", programId='" + programId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", logo='" + logo + '\'' +
                ", image='" + image + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", category='" + category + '\'' +
                '}'
    }
}