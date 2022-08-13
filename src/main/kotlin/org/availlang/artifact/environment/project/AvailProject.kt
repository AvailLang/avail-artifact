/*
 * AvailProjectConfiguration.kt
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

package org.availlang.artifact.environment.project

import org.availlang.json.JSONFriendly
import org.availlang.json.JSONObject
import org.availlang.json.JSONWriter
import java.util.UUID

/**
 * Describes the makeup of an Avail project.
 *
 * This also implements a [JSONFriendly] interface for writing this as a
 * configuration file used for starting up Avail project environments.
 *
 * @author Richard Arriaga
 *
 * @property name
 *   The name of the Avail project.
 * @property darkMode
 *   `true` indicates use of Avail Workbench's dark mode; `false` for light
 *   mode.
 * @property repositoryLocation
 *   The [ProjectLocation] for the Avail repository where a persistent Avail
 *   indexed file of compiled modules are stored.
 * @property id
 *   The id that uniquely identifies the project.
 * @property roots
 *   The map of [AvailProjectRoot.name] to [AvailProjectRoot].
 */
class AvailProject constructor(
	val name: String,
	val darkMode: Boolean,
	val repositoryLocation: ProjectLocation,
	val id: String = UUID.randomUUID().toString(),
	val roots: MutableMap<String, AvailProjectRoot> = mutableMapOf()
): JSONFriendly
{
	/**
	 * The list of [AvailProjectRoot]s in this [AvailProject].
	 */
	val availProjectRoots: List<AvailProjectRoot> get() =
		roots.values.toList().sortedBy { it.name }

	/**
	 * Add the [AvailProjectRoot] to this [AvailProject].
	 *
	 * @param availProjectRoot
	 *   The `AvailProjectRoot` to add.
	 */
	fun addRoot (availProjectRoot: AvailProjectRoot)
	{
		roots[availProjectRoot.id] = availProjectRoot
	}

	/**
	 * Remove the [AvailProjectRoot] from this [AvailProject].
	 *
	 * @param projectRoot
	 *   The [AvailProjectRoot.id] to remove.
	 * @return
	 *   The `AvailProjectRoot` removed or `null` if not found.
	 */
	fun removeRoot (projectRoot: String): AvailProjectRoot? =
		roots.remove(projectRoot)

	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(AvailProject::id.name) { write(id) }
			at(AvailProject::darkMode.name) { write(darkMode) }
			at(SERIALIZATION_VERSION_JSON_FIELD) {
				write(CURRENT_SERIALIZATION_VERSION)
			}
			at(AvailProject::name.name) { write(name) }
			at(AvailProject::repositoryLocation.name)
			{
				write(repositoryLocation)
			}
			at(AvailProject::roots.name)
			{
				startArray()
				availProjectRoots.forEach {
					startObject()
					it.writeTo(writer)
					endObject()
				}
				endArray()
			}
		}
	}

	companion object
	{
		/**
		 * The field name of field in the JSON file that contains the version
		 * of serialization the file was created with.
		 */
		const val SERIALIZATION_VERSION_JSON_FIELD = "serializationVersion"

		/**
		 * The current JSON serialization/deserialization version of
		 * [AvailProject].
		 */
		const val CURRENT_SERIALIZATION_VERSION = 1

		/**
		 * The Avail configuration file name.
		 */
		const val CONFIG_FILE_NAME = "config/avail-config.json"

		/**
		 * Extract and build a [AvailProject] from the provided
		 * [JSONObject].
		 *
		 * @param projectDirectory
		 *   The root directory of the project.
		 * @param jsonObject
		 *   The `JSONObject` that contains the `ProjectDescriptor` data.
		 * @return
		 *   The extracted `ProjectDescriptor`.
		 */
		fun from (
			projectDirectory: String,
			jsonObject: JSONObject
		): AvailProject
		{
			val id = jsonObject.getString(AvailProject::id.name)
			val name = jsonObject.getString(AvailProject::name.name)
			val darkMode =
				jsonObject.getBoolean(AvailProject::darkMode.name)
			val repoLocation = ProjectLocation.from(
				projectDirectory,
				jsonObject.getObject(
					AvailProject::repositoryLocation.name))
			val roots = mutableMapOf<String, AvailProjectRoot>()
			val projectProblems = mutableListOf<ProjectProblem>()
			jsonObject.getArray(AvailProject::roots.name)
				.forEachIndexed { i, it ->
					val rootObj = it as? JSONObject ?: run {
						projectProblems.add(
							ConfigFileProblem(
								"Malformed Avail project config file, " +
									"$CONFIG_FILE_NAME; malformed " +
									AvailProject::roots.name +
									" object at position $i"))
						return@forEachIndexed
					}
					val root =
						try
						{
							AvailProjectRoot.from(projectDirectory, rootObj)
						}
						catch (e: Throwable)
						{
							projectProblems.add(
								ConfigFileProblem(
									"Malformed Avail project config" +
										" file, $CONFIG_FILE_NAME; malformed " +
										AvailProject::roots.name +
										" object at position $i"))
							return@forEachIndexed
						}
					roots[root.id] = root
				}
			return AvailProject(
				name, darkMode, repoLocation, id, roots)
		}
	}
}
