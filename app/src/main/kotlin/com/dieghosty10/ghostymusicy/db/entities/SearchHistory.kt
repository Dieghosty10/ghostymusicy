/*
 * ghostymusicy Project Original (2026)
 * Dieghosty10 (github.com/Dieghosty10)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.dieghosty10.ghostymusicy.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "search_history",
    indices = [
        Index(
            value = ["query"],
            unique = true,
        ),
    ],
)
data class SearchHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
)
