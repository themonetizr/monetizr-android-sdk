package io.monetizr.monetizrsdk

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Taken advice and implementation from https://github.com/florent37/ApplicationProvider
 *
 * This approach allows not to initialize component, but get context from application on its launch.
 * It allows for easier implementation without application override
 */
abstract class EmptyProvider : ContentProvider(){
    override fun insert(uri: Uri, values: ContentValues?): Uri {
        throw Exception("unimplemented")
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        throw Exception("unimplemented")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw Exception("unimplemented")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw Exception("unimplemented")
    }

    override fun getType(uri: Uri): String {
        throw Exception("unimplemented")
    }
}