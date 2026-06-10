import java.io.File

fun main() {
    val file = File("app/src/main/kotlin/com/dieghosty10/ghostymusicy/viewmodels/SearchViewModel.kt")
    val content = file.readText()
    val regex = Regex("is com\\.dieghosty10\\.ghostymusicy\\.innertube\\.models\\.AlbumItem -> \\{[^}]*\\}\\s*else -> \\{[^}]*if \\(item is com\\.dieghosty10\\.ghostymusicy\\.innertube\\.models\\.AlbumItem\\) \\{[^}]*\\}[^}]*\\}", RegexOption.DOT_MATCHES_ALL)
    
    val newBlock = ""
                  is com.dieghosty10.ghostymusicy.innertube.models.AlbumItem -> {
                      insert(
                          com.dieghosty10.ghostymusicy.db.entities.AlbumEntity(
                              id = item.browseId,
                              playlistId = item.playlistId,
                              title = item.title,
                              year = item.year,
                              thumbnailUrl = item.thumbnail,
                              songCount = 0,
                              duration = 0,
                              inLibrary = java.time.LocalDateTime.now()
                          )
                      )
                  }
                  else -> {}""
                  
    val newContent = content.replace(regex, newBlock)
    file.writeText(newContent)
}
