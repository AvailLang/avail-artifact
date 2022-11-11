@file:Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")

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
 *   `true` indicates the root is intended to be displayed; `false` indicates
 *   the root should not be visible by default.
 * @property stylesheet
 *   The default stylesheet for this root.
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
	val stylesheet: Map<String, StyleAttributes> = mapOf()
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
			at(::id.name) { write(id) }
			at(::name.name) { write(name) }
			at(::editable.name) { write(editable) }
			at(::visible.name) { write(visible) }
			at(::location.name) { location.writeTo(this@writeObject) }
			at(::availModuleExtensions.name) {
				writeStrings(availModuleExtensions)
			}
			if (templates.isNotEmpty())
			{
				at(::templates.name) {
					writeObject {
						templates.forEach { (name, expansion) ->
							at(name) { write(expansion) }
						}
					}
				}
			}
			if (stylesheet.isNotEmpty())
			{
				at(::stylesheet.name) {
					writeObject {
						stylesheet.forEach { (rule, attributes) ->
							at(rule) { write(attributes) }
						}
					}
				}
			}
			at(::rootCopyright.name) { write(rootCopyright) }
		}
	}

	override fun toString(): String = "$name=${location.fullPath}"

	companion object
	{
		/**
		 * Extract and build an [AvailProjectRoot] from the provided
		 * [JSONObject].
		 *
		 * @param projectDirectory
		 *   The root directory of this project.
		 * @param obj
		 *   The [JSONObject] that contains the [AvailProjectRoot] data.
		 * @param serializationVersion
		 *   The [Int] identifying the version of the [AvailProject] within
		 *   which this is a root.
		 * @return
		 *   The extracted `ProjectRoot`.
		 */
		fun from (
			projectDirectory: String,
			obj: JSONObject,
			@Suppress("UNUSED_PARAMETER") serializationVersion: Int
		) = AvailProjectRoot(
			projectDirectory,
			obj.getString(AvailProjectRoot::name.name),
			AvailLocation.from(
				projectDirectory,
				obj.getObject(AvailProjectRoot::location.name)),
			obj.getArray(AvailProjectRoot::availModuleExtensions.name).strings,
			obj.getObjectOrNull(AvailProjectRoot::templates.name)?.let {
				it.associateTo(mutableMapOf()) { (name, expansion) ->
					name to expansion.string
				}
			} ?: mapOf(),
			obj.getBoolean(AvailProjectRoot::editable.name),
			obj.getString(AvailProjectRoot::id.name),
			obj.getString(AvailProjectRoot::rootCopyright.name) { "" },
			obj.getBoolean(AvailProjectRoot::visible.name) { true },
			obj.getObjectOrNull(AvailProjectRoot::stylesheet.name)?.let {
				it.associateTo(mutableMapOf()) { (rule, attributes) ->
					rule to StyleAttributes(attributes as JSONObject)
				}
			} ?: mapOf())
	}
}
