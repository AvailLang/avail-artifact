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

import org.availlang.artifact.environment.location.AvailLocation
import org.availlang.artifact.environment.project.AvailProject.Companion.CONFIG_FILE_NAME
import org.availlang.json.JSONObject
import org.availlang.json.JSONWriter
import java.util.UUID

/**
 * Describes [AvailProject.serializationVersion] 1 of an [AvailProject].
 *
 * @author Richard Arriaga
 *
 * @constructor
 * Construct an [AvailProjectV1].
 *
 * @param name
 *   The name of the Avail project.
 * @param darkMode
 *   `true` indicates use of Avail Workbench's dark mode; `false` for light
 *   mode.
 * @param repositoryLocation
 *   The [AvailLocation] for the Avail repository where a persistent Avail
 *   indexed file of compiled modules are stored.
 * @param id
 *   The id that uniquely identifies the project.
 * @property roots
 *   The map of [AvailProjectRoot.name] to [AvailProjectRoot].
 */
class AvailProjectV1 constructor(
	override val name: String,
	override val darkMode: Boolean,
	override val repositoryLocation: AvailLocation,
	override val id: String = UUID.randomUUID().toString(),
	override val roots: MutableMap<String, AvailProjectRoot> = mutableMapOf()
): AvailProject
{
	override val serializationVersion: Int = 1

	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(AvailProjectV1::id.name) { write(id) }
			at(AvailProjectV1::darkMode.name) { write(darkMode) }
			at(AvailProjectV1::serializationVersion.name) {
				write(serializationVersion)
			}
			at(AvailProjectV1::name.name) { write(name) }
			at(AvailProjectV1::repositoryLocation.name)
			{
				write(repositoryLocation)
			}
			at(AvailProjectV1::roots.name)
			{
				startArray()
				availProjectRoots.forEach {
					it.writeTo(writer)
				}
				endArray()
			}
		}
	}

	companion object
	{
		/**
		 * Extract and build a [AvailProjectV1] from the provided
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
		): AvailProjectV1
		{
			val id = jsonObject.getString(AvailProjectV1::id.name)
			val name = jsonObject.getString(AvailProjectV1::name.name)
			val darkMode =
				jsonObject.getBoolean(AvailProjectV1::darkMode.name)
			val repoLocation = AvailLocation.from(
				projectDirectory,
				jsonObject.getObject(
					AvailProjectV1::repositoryLocation.name))
			val roots = mutableMapOf<String, AvailProjectRoot>()
			val projectProblems = mutableListOf<ProjectProblem>()
			jsonObject.getArray(AvailProjectV1::roots.name)
				.forEachIndexed { i, it ->
					val rootObj = it as? JSONObject ?: run {
						projectProblems.add(
							ConfigFileProblem(
								"Malformed Avail project config file, " +
									"$CONFIG_FILE_NAME; malformed " +
									AvailProjectV1::roots.name +
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
										AvailProjectV1::roots.name +
										" object at position $i"))
							return@forEachIndexed
						}
					roots[root.id] = root
				}
			return AvailProjectV1(name, darkMode, repoLocation, id, roots)
		}
	}
}
