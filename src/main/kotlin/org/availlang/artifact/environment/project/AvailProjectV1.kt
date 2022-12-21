/*
 * AvailProjectV1.kt
 * Copyright © 1993-2022, The Avail Foundation, LLC.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of the copyright holder nor the names of the contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

@file:Suppress("DuplicatedCode")

package org.availlang.artifact.environment.project

import org.availlang.artifact.environment.location.AvailLocation
import org.availlang.artifact.environment.project.AvailProject.Companion.CONFIG_FILE_NAME
import org.availlang.json.JSONObject
import org.availlang.json.JSONWriter
import java.util.UUID

/**
 * Describes [AvailProject.serializationVersion] 1 of an [AvailProject].
 *
 * @property name
 *   The name of the Avail project.
 * @property darkMode
 *   `true` indicates use of Avail Workbench's dark mode; `false` for light
 *   mode.
 * @property repositoryLocation
 *   The [AvailLocation] for the Avail repository where a persistent Avail
 *   indexed file of compiled modules are stored.
 * @property id
 *   The id that uniquely identifies the project.
 * @property roots
 *   The map of [AvailProjectRoot.name] to [AvailProjectRoot].
 * @property templates
 *   The templates that should be available when editing Avail source modules
 *   in the workbench.
 * @property projectCopyright
 *   The copyright to prepend to new Avail modules in this project.
 * @property palette
 *   The [Palette] for the accompanying stylesheet, against which symbolic names
 *   are resolved.
 * @property stylesheet
 *   The default stylesheet for this root. Symbolic names are resolved against
 *   the accompanying [Palette].
 * @author Richard Arriaga
 */
class AvailProjectV1 constructor(
	override val name: String,
	override val darkMode: Boolean,
	override val repositoryLocation: AvailLocation,
	override val id: String = UUID.randomUUID().toString(),
	override val roots: MutableMap<String, AvailProjectRoot> = mutableMapOf(),
	override val templates: MutableMap<String, String> = mutableMapOf(),
	override var projectCopyright: String = "",
	override val palette: Palette = Palette.empty,
	override val stylesheet: Map<String, StyleAttributes> = mutableMapOf()
): AvailProject
{
	override val serializationVersion = AvailProjectV1.serializationVersion

	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(::id.name) { write(id) }
			at(::darkMode.name) { write(darkMode) }
			at(::serializationVersion.name) {
				write(serializationVersion)
			}
			at(::name.name) { write(name) }
			at(::repositoryLocation.name) {
				write(repositoryLocation)
			}
			at(::roots.name) { writeArray(availProjectRoots) }
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
			at(::projectCopyright.name) {
				write(projectCopyright)
			}
		}
	}

	companion object
	{
		/** The serialization version (for access without an instance). */
		const val serializationVersion: Int = 1

		/**
		 * Extract and build a [AvailProjectV1] from the provided
		 * [JSONObject].
		 *
		 * @param projectDirectory
		 *   The root directory of the project.
		 * @param obj
		 *   The `JSONObject` that contains the `ProjectDescriptor` data.
		 * @return
		 *   The extracted `ProjectDescriptor`.
		 */
		fun from (
			projectDirectory: String,
			obj: JSONObject
		): AvailProjectV1
		{
			val id = obj.getString(AvailProjectV1::id.name)
			val name = obj.getString(AvailProjectV1::name.name)
			val darkMode = obj.getBooleanOrNull(AvailProjectV1::darkMode.name)
				?: true
			val repoLocation = AvailLocation.from(
				projectDirectory,
				obj.getObject(AvailProjectV1::repositoryLocation.name))
			val templates =  obj.getObjectOrNull(
				AvailProjectV1::templates.name
			)?.let { o ->
				o.map { (name, expansion) -> name to expansion.string }
					.associate { it }
			}?.toMutableMap() ?: mutableMapOf()
			val projectProblems = mutableListOf<ProjectProblem>()
			val roots = obj.getArray(
				AvailProjectV1::roots.name
			).mapIndexedNotNull { i, it ->
				val rootObj = it as? JSONObject ?: run {
					projectProblems.add(
						ConfigFileProblem(
							"Malformed Avail project config file, "
								+ "$CONFIG_FILE_NAME; malformed "
								+ AvailProjectV1::roots.name
								+ " object at position $i"))
					return@mapIndexedNotNull null
				}
				return@mapIndexedNotNull try
				{
					val root = AvailProjectRoot.from(
						projectDirectory,
						rootObj,
						serializationVersion
					)
					root.name to root
				}
				catch (e: Throwable)
				{
					projectProblems.add(
						ConfigFileProblem(
							"Malformed Avail project config"
								+ " file, $CONFIG_FILE_NAME; malformed "
								+ AvailProjectV1::roots.name
								+ " object at position $i"))
					null
				}
			}.associateTo(mutableMapOf()) { it }
			val palette = obj.getObjectOrNull(
				AvailProjectV1::palette.name
			)?.let {
				Palette.from(it)
			} ?: Palette.empty
			val stylesheet = obj.getObjectOrNull(
				AvailProjectV1::stylesheet.name
			)?.let { o ->
				o.map { (rule, attributes) ->
					rule to StyleAttributes(attributes as JSONObject)
				}.associate { it }
			} ?: mapOf()
			val copyright = obj.getString(
				AvailProjectV1::projectCopyright.name
			) { "" }
			if (projectProblems.isNotEmpty())
			{
				throw AvailProjectException(projectProblems)
			}
			return AvailProjectV1(
				name = name,
				darkMode = darkMode,
				repositoryLocation = repoLocation,
				id = id,
				roots = roots,
				templates = templates,
				projectCopyright = copyright,
				palette = palette,
				stylesheet = stylesheet
			)
		}
	}
}

/**
 * An [AvailProjectException] is raised by a [factory][AvailProjectV1] that
 * creates an [AvailProject] from a persistent representation.
 *
 * @author Todd L Smith &lt;todd@availlang.org&gt;
 */
data class AvailProjectException constructor(
	val problems: List<ProjectProblem>
): Exception()
