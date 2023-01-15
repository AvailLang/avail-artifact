@file:Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")

package org.availlang.artifact.environment.project

import org.availlang.artifact.AvailArtifact
import org.availlang.artifact.environment.location.AvailLocation
import org.availlang.artifact.jar.AvailArtifactJar
import org.availlang.artifact.manifest.AvailRootManifest
import org.availlang.artifact.roots.AvailRoot
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
 * @property palette
 *   The [Palette] for the accompanying stylesheet, against which symbolic names
 *   are resolved.
 * @property stylesheet
 *   The default stylesheet for this root. Symbolic names are resolved against
 *   the accompanying [Palette].
 */
class AvailProjectRoot constructor(
	val projectDirectory: String,
	var name: String,
	var location: AvailLocation,
	val availModuleExtensions: MutableList<String> = mutableListOf("avail"),
	val templates: MutableMap<String, TemplateExpansion> = mutableMapOf(),
	var editable: Boolean = location.editable,
	val id: String = UUID.randomUUID().toString(),
	var rootCopyright: String = "",
	var visible: Boolean = true,
	val palette: Palette = Palette.empty,
	val stylesheet: Map<String, StyleAttributes> = mutableMapOf()
): JSONFriendly
{
	/**
	 * Specific for root imported from an [AvailArtifact] library, specifically
	 * an [AvailArtifactJar].
	 *
	 * `true` indicates styles will not be overriden when loading root from the
	 * artifact; `false` indicates they will.
	 */
	var lockPalette: Boolean = false

	/**
	 * Specific for root imported from an [AvailArtifact] library, specifically
	 * an [AvailArtifactJar].
	 *
	 * `true` indicates the templates will be not be overriden when loading root
	 * from the artifact; `false` indicates they will.
	 */
	var lockTemplates: Boolean = false

	/**
	 * The Avail module descriptor path. It takes the form:
	 *
	 * `"$name=$uri"`
	 */
	@Suppress("unused")
	val modulePath: String = "$name=${location.fullPath}"

	/**
	 * Create an [AvailRootManifest] from this [AvailProjectRoot].
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	val manifest: AvailRootManifest
		get() =
		AvailRootManifest(
			name,
			availModuleExtensions,
			templates = templates
				.filter { it.value.markedForArtifactInclusion }
				.toMutableMap(),
			stylesheet = stylesheet,
			palette = palette)

	/**
	 * The [AvailRoot] sourced from this [AvailProjectRoot].
	 */
	@Suppress("unused")
	val availRoot: AvailRoot get() =
		AvailRoot(
			name = name,
			location = location,
			availModuleExtensions = availModuleExtensions,
			templates = templates,
			stylesheet = stylesheet,
			palette = palette)

	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(::id.name) { write(id) }
			at(::name.name) { write(name) }
			at(::editable.name) { write(editable) }
			at(::visible.name) { write(visible) }
			at(::location.name) { location.writeTo(this@writeObject) }
			if (availModuleExtensions != listOf("avail"))
			{
				at(::availModuleExtensions.name) {
					writeStrings(availModuleExtensions)
				}
			}
			at(::lockTemplates.name) { write(lockTemplates) }
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
			at(::lockPalette.name) { write(lockPalette) }
			if (palette.isNotEmpty)
			{
				at(::palette.name) { write(palette) }
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
			projectDirectory = projectDirectory,
			name = obj.getString(AvailProjectRoot::name.name),
			location = AvailLocation.from(
				projectDirectory,
				obj.getObject(AvailProjectRoot::location.name)
			),
			availModuleExtensions = obj.getArrayOrNull(
				AvailProjectRoot::availModuleExtensions.name
			)?.strings?.toMutableList() ?: mutableListOf("avail"),
			templates = obj.getObjectOrNull(
				AvailProjectRoot::templates.name
			)?.let {
				it.associateTo(mutableMapOf()) { (name, expansion) ->
					name to TemplateExpansion(expansion as JSONObject)
				}
			} ?: mutableMapOf(),
			editable = obj.getBoolean(
				AvailProjectRoot::editable.name
			) { false },
			id = obj.getString(AvailProjectRoot::id.name),
			rootCopyright = obj.getString(
				AvailProjectRoot::rootCopyright.name
			) { "" },
			visible = obj.getBoolean(AvailProjectRoot::visible.name) { true },
			palette = obj.getObjectOrNull(
				AvailProjectRoot::palette.name
			)?.let {
				Palette.from(it)
			} ?: Palette.empty,
			stylesheet = obj.getObjectOrNull(
				AvailProjectRoot::stylesheet.name
			)?.let {
				it.associateTo(mutableMapOf()) { (rule, attributes) ->
					rule to StyleAttributes(attributes as JSONObject)
				}
			} ?: mutableMapOf()
		).apply {
			obj.getBooleanOrNull(::lockTemplates.name)?.let {
				lockTemplates = it
			}
			obj.getBooleanOrNull(::lockPalette.name)?.let {
				lockPalette = it
			}
		}
	}
}
