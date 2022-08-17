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
 * @property templates
 *   The templates that should be available when editing Avail source
 *   modules in the workbench.
 * @property editable
 *   `true` indicates this root is editable by the project; `false` otherwise.
 * @property id
 *   The immutable id that uniquely identifies this [AvailProjectRoot].
 * @property rootCopyright
 *   The copyright to prepend to new Avail modules in this root.
 * @property visible
 *   `true` indicates the root is intended to be displayed; `false` indciates
 *   the root should not be visible by default.
 */
class AvailProjectRoot constructor(
	val projectDirectory: String,
	var name: String,
	var location: AvailLocation,
	val availModuleExtensions: List<String> = listOf("avail"),
	val templates: Map<String, String> = mapOf(),
	var editable: Boolean = location.editable,
	val id: String = UUID.randomUUID().toString(),
	var rootCopyright: String = "",
	var visible: Boolean = true,
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
			at(AvailProjectRoot::visible.name) { write(visible) }
			at(AvailProjectRoot::location.name) {
				location.writeTo(this@writeObject)
			}
			at(AvailProjectRoot::availModuleExtensions.name) {
				writeStrings(availModuleExtensions)
			}
			if (templates.isNotEmpty())
			{
				at(AvailProjectRoot::templates.name) {
					writeObject {
						templates.forEach { (name, expansion) ->
							at(name) { write(expansion) }
						}
					}
				}
			}
			at(AvailProjectRoot::rootCopyright.name) { write(rootCopyright) }
		}
	}

	override fun toString(): String = modulePath

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
				run {
					if (obj.containsKey(AvailProjectRoot::templates.name))
					{
						val templates = mutableMapOf<String, String>()
						val map = obj.getObject(
							AvailProjectRoot::templates.name)
						map.forEach { (name, expansion) ->
							templates[name] = expansion.string
						}
						templates
					}
					else mapOf<String, String>()
				},
				obj.getBoolean(AvailProjectRoot::editable.name),
				obj.getString(AvailProjectRoot::id.name),
				if (obj.containsKey(AvailProjectRoot::rootCopyright.name))
				{
					obj.getString(AvailProjectRoot::rootCopyright.name)
				}
				else "",
				obj.getBoolean(AvailProjectRoot::visible.name),)
	}
}
