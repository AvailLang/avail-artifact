package org.availlang.artifact.environment.project

import org.availlang.json.JSONFriendly
import org.availlang.json.JSONObject
import org.availlang.json.JSONWriter
import java.util.*

/**
 * Represents an Avail module root in a [AvailProject].
 *
 * @author Richard Arriaga
 *
 * @property projectDirectory
 *   The root directory of this project.
 * @property name
 *   The name of the module root.
 * @property location
 *   The [ProjectLocation] of this root.
 * @property editable
 *   `true` indicates this root is editable by the project; `false` otherwise.
 * @property id
 *   The immutable id that uniquely identifies this [AvailProjectRoot].
 */
class AvailProjectRoot constructor(
	val projectDirectory: String,
	val scheme: String,
	var name: String,
	var location: ProjectLocation,
	var editable: Boolean = location.editable,
	val id: String = UUID.randomUUID().toString()
): JSONFriendly
{
	/**
	 * The Avail module descriptor path. It takes the form:
	 *
	 * `"$name=$uri"`
	 */
	val modulePath: String = "$name=$scheme${location.fullPath(projectDirectory)}"

	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(AvailProjectRoot::scheme.name) { write(scheme) }
			at(AvailProjectRoot::id.name) { write(id) }
			at(AvailProjectRoot::name.name) { write(name) }
			at(AvailProjectRoot::editable.name) { write(editable) }
			at(AvailProjectRoot::location.name) { write(location) }
		}
	}

	companion object
	{
		/**
		 * Extract and build a [AvailProjectRoot] from the provided [JSONObject].
		 *
		 * @param projectDirectory
		 *   The root directory of this project.
		 * @param jsonObject
		 *   The `JSONObject` that contains the `ProjectRoot` data.
		 * @return
		 *   The extracted `ProjectRoot`.
		 */
		fun from (
			projectDirectory: String,
			jsonObject: JSONObject
		): AvailProjectRoot =
			AvailProjectRoot(
				projectDirectory,
				jsonObject.getString(AvailProjectRoot::scheme.name),
				jsonObject.getString(AvailProjectRoot::name.name),
				ProjectLocation.from(
					jsonObject.getString(AvailProjectRoot::projectDirectory.name),
					jsonObject.getObject(AvailProjectRoot::location.name)),
				jsonObject.getBoolean(AvailProjectRoot::editable.name),
				jsonObject.getString(AvailProjectRoot::id.name))
	}
}
