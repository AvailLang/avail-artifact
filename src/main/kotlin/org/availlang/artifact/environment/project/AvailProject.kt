/*
 * AvailProject.kt
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

import org.availlang.artifact.AvailArtifactException
import org.availlang.artifact.environment.location.AvailLocation
import org.availlang.artifact.manifest.AvailArtifactManifest
import org.availlang.json.JSONFriendly
import org.availlang.json.JSONObject
import org.availlang.json.jsonPrettyPrintWriter

/**
 * Describes the makeup of an Avail project.
 *
 * This also implements a [JSONFriendly] interface for writing this as a
 * configuration file used for starting up Avail project environments.
 *
 * @author Richard Arriaga
 */
interface AvailProject: JSONFriendly
{
	/**
	 * The name of the Avail project.
	 */
	val name: String

	/**
	 * The serialization version of this [AvailProject] which represents the
	 * structure of the [JSONFriendly]-based configuration file that represents
	 * this [AvailProject].
	 */
	val serializationVersion: Int

	/**
	 * The [AvailLocation] for the Avail repository where a persistent Avail
	 * indexed file of compiled modules are stored.
	 */
	val repositoryLocation: AvailLocation

	/**
	 * The id that uniquely identifies the project.
	 */
	val id: String

	/**
	 * `true` indicates use of Avail Workbench's dark mode; `false` for light
	 *  mode.
	 */
	val darkMode: Boolean

	/**
	 * The map of [AvailProjectRoot.name] to [AvailProjectRoot].
	 */
	val roots: MutableMap<String, AvailProjectRoot>

	/**
	 * The templates that should be available when editing Avail source modules
	 * in the workbench, as a map from template names (corresponding to user
	 * inputs) to template expansions. Zero or one caret insertion (⁁) may
	 * appear in each expansion.
	 */
	val templates: Map<String, String>

	/**
	 * The project-specified [palette]][Palette], overriding any root palettes.
	 */
	val palette: Palette

	/** The project-specific stylesheet, overriding any root stylesheets. */
	val stylesheet: Map<String, StyleAttributes>

	/**
	 * The copyright to prepend to new Avail modules by default.
	 */
	var projectCopyright: String

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
	@Suppress("unused")
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
	@Suppress("unused")
	fun removeRoot (projectRoot: String): AvailProjectRoot? =
		roots.remove(projectRoot)

	/**
	 * The String file contents of this [AvailArtifactManifest].
	 */
	@Suppress("unused")
	val fileContent: String get() =
		jsonPrettyPrintWriter {
			this@AvailProject.writeTo(this)
		}.toString()

	companion object
	{
		/**
		 * The version that represents the current structure under which Avail
		 * libs are packaged in the artifact.
		 */
		private const val CURRENT_PROJECT_VERSION = 1

		/**
		 * The Avail configuration file name.
		 */
		const val CONFIG_FILE_NAME = "avail-config.json"

		/**
		 * The name of the directory where the roots are stored for an
		 * [AvailProject].
		 */
		@Suppress("unused")
		const val ROOTS_DIR = "roots"

		/**
		 * Extract and build a [AvailProject] from the provided [JSONObject].
		 *
		 * @param projectDirectory
		 *   The root directory of the project.
		 * @param obj
		 *   The `JSONObject` that contains the [AvailProject] data.
		 * @return
		 *   The extracted `AvailProject`.
		 */
		fun from (
			projectDirectory: String,
			obj: JSONObject
		): AvailProject
		{
			val version =
				obj.getNumber(AvailProject::serializationVersion.name).int

			return when (version)
			{
				1 -> AvailProjectV1.from(projectDirectory, obj)
				else ->
					throw AvailArtifactException("Invalid Avail Project: " +
						"Version $version is not in the valid range of " +
						"known project versions," +
						" [1, $CURRENT_PROJECT_VERSION].")
			}
		}
	}
}
