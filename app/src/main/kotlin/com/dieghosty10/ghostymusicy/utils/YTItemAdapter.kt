package com.dieghosty10.ghostymusicy.utils

import com.dieghosty10.ghostymusicy.innertube.models.AlbumItem
import com.dieghosty10.ghostymusicy.innertube.models.ArtistItem
import com.dieghosty10.ghostymusicy.innertube.models.PlaylistItem
import com.dieghosty10.ghostymusicy.innertube.models.SongItem
import com.dieghosty10.ghostymusicy.innertube.models.YTItem
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class YTItemAdapter : JsonSerializer<YTItem>, JsonDeserializer<YTItem> {
    override fun serialize(src: YTItem, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val json = context.serialize(src).asJsonObject
        json.addProperty("YT_TYPE", src.javaClass.simpleName)
        return json
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): YTItem {
        val obj = json.asJsonObject
        val type = obj.get("YT_TYPE")?.asString ?: "SongItem"
        return when (type) {
            "SongItem" -> context.deserialize(obj, SongItem::class.java)
            "AlbumItem" -> context.deserialize(obj, AlbumItem::class.java)
            "ArtistItem" -> context.deserialize(obj, ArtistItem::class.java)
            "PlaylistItem" -> context.deserialize(obj, PlaylistItem::class.java)
            else -> context.deserialize(obj, SongItem::class.java)
        }
    }
}
