package com.lizongying.mytv0.data

import java.io.Serializable

data class TV(
    var id: Int = -1,
    var name: String = "",
    var title: String = "",
    var description: String? = null,
    var logo: String = "",
    var image: String? = null,
    var uris: List<String>,
    var videoIndex: Int = 0,
    var headers: Map<String, String>? = null,
    var group: String = "",
    var sourceType: SourceType = SourceType.UNKNOWN,
    var child: List<TV>,
) : Serializable {

    override fun toString(): String {
        return "TV{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", logo='" + logo + '\'' +
                ", image='" + image + '\'' +
                ", uris='" + uris + '\'' +
                ", headers='" + headers + '\'' +
                ", group='" + group + '\'' +
                ", sourceType='" + sourceType + '\'' +
                '}'
    }
}