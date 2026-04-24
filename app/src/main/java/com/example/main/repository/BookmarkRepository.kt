package com.example.main.repository

import android.content.Context

class BookmarkRepository(context: Context) {

    private val prefs = context.getSharedPreferences("bookmark_prefs", Context.MODE_PRIVATE)

    fun getBookmarks(): MutableSet<String> =
        prefs.getStringSet("bookmarks", emptySet())!!.toMutableSet()

    fun isBookmarked(name: String): Boolean =
        getBookmarks().contains(name)

    fun toggleBookmark(name: String): Boolean {
        val bookmarks = getBookmarks()
        if (bookmarks.contains(name)) bookmarks.remove(name) else bookmarks.add(name)
        prefs.edit().putStringSet("bookmarks", bookmarks).apply()
        return bookmarks.contains(name)
    }

    fun saveBookmarks(bookmarks: Set<String>) {
        prefs.edit().putStringSet("bookmarks", bookmarks).apply()
    }
}
