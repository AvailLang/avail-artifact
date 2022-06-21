package org.availlang.artifact.configuration

import org.availlang.artifact.AvailArtifactException
import org.availlang.json.JSONFriendly
import org.availlang.json.JSONObject
import org.availlang.json.JSONWriter

/**
 * A directive to rename an Avail Module Root to a different name upon loading.
 *
 * @author Richard Arriaga
 *
 * @property originalName
 *   The name originally provided the module root.
 * @property rename
 *   The new name to provide to the module root.
 */
class AvailRootRename constructor(
	val originalName: String,
	val rename: String
): JSONFriendly
{
	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(AvailRootRename::originalName.name)
			{
				write(originalName)
			}
			at(AvailRootRename::rename.name)
			{
				write(rename)
			}
		}
	}

	companion object
	{
		internal fun fromJSON (obj: JSONObject): AvailRootRename
		{
			val originalName =
				try
				{
					obj.getString(AvailRootRename::originalName.name)
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem accessing AvailRootRename originalName.",
						e)
				}
			val rename =
				try
				{
					obj.getString(AvailRootRename::rename.name)
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem accessing AvailRootRename rename.",
						e)
				}
			return AvailRootRename(originalName, rename)
		}
	}
}
