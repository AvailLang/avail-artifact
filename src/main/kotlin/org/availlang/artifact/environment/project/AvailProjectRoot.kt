package org.availlang.artifact.environment.project

import org.availlang.artifact.environment.location.AvailLocation
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
 *   The root directory of the [AvailProject].
 * @property name
 *   The name of the module root.
 * @property location
 *   The [AvailLocation] of this root.
 * @property availModuleExtensions
 *   The file extensions that signify files that should be treated as Avail
 *   modules.
 * @property editable
 *   `true` indicates this root is editable by the project; `false` otherwise.
 * @property id
 *   The immutable id that uniquely identifies this [AvailProjectRoot].
 */
class AvailProjectRoot constructor(
	val projectDirectory: String,
	var name: String,
	var location: AvailLocation,
	val availModuleExtensions: List<String> = listOf("avail"),
	var editable: Boolean = location.editable,
	val id: String = UUID.randomUUID().toString()
): JSONFriendly
{
	/**
	 * The Avail module descriptor path. It takes the form:
	 *
	 * `"$name=$uri"`
	 */
	@Suppress("unused")
	val modulePath: String = "$name=${location.fullPath}"

	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(AvailProjectRoot::id.name) { write(id) }
			at(AvailProjectRoot::name.name) { write(name) }
			at(AvailProjectRoot::editable.name) { write(editable) }
			at(AvailProjectRoot::location.name) {
				location.writeTo(this@writeObject)
			}
			at(AvailProjectRoot::availModuleExtensions.name) {
				writeStrings(availModuleExtensions)
			}
		}
	}

	companion object
	{
		/**
		 * Extract and build a [AvailProjectRoot] from the provided [JSONObject].
		 *
		 * @param projectDirectory
		 *   The root directory of this project.
		 * @param obj
		 *   The `JSONObject` that contains the `ProjectRoot` data.
		 * @return
		 *   The extracted `ProjectRoot`.
		 */
		fun from (
			projectDirectory: String,
			obj: JSONObject
		): AvailProjectRoot =
			AvailProjectRoot(
				projectDirectory,
				obj.getString(AvailProjectRoot::name.name),
				AvailLocation.from(
					projectDirectory,
					obj.getObject(AvailProjectRoot::location.name)),
				obj.getArray(
					AvailProjectRoot::availModuleExtensions.name).strings,
				obj.getBoolean(AvailProjectRoot::editable.name),
				obj.getString(AvailProjectRoot::id.name))
	}
}
