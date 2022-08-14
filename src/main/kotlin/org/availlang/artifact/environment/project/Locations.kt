/*
 * Locations.kt
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

import org.availlang.artifact.environment.AvailEnvironment
import org.availlang.json.JSONFriendly
import org.availlang.json.JSONObject
import org.availlang.json.JSONWriter
import java.net.URI

/**
 * The supported [URI.scheme]s.
 *
 * @author Richard Arriaga
 */
enum class Scheme constructor(val prefix: String)
{
	/**
	 * The canonical representation of an invalid scheme.
	 */
	INVALID(""),

	/**
	 * A file location.
	 */
	FILE("file://"),

	/**
	 * A JAR file.
	 */
	JAR("jar:/")
}

/**
 * Represents a location of a URI location for something related to an
 * [AvailEnvironment] project.
 *
 * @author Richard Arriaga
 *
 * @property locationType
 *   The [LocationType] member that represents the type of this location.
 * @property scheme
 *   The [Scheme] for this location.
 * @property path
 *   The path to this location.
 */
sealed class AvailLocation constructor(
	val locationType: LocationType,
	val scheme: Scheme,
	val path: String
): JSONFriendly
{
	/**
	 * Answer the full path to the location.
	 */
	val fullPath: String get() = "${scheme.prefix}$fullPathNoPrefix"

	/**
	 * Answer the full path to the location.
	 */
	abstract val fullPathNoPrefix: String

	/**
	 * Are the contents of this location editable by this project? `true`
	 * indicates it is; `false` otherwise.
	 */
	open val editable: Boolean = false

	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(AvailLocation::locationType.name) { write(locationType.name) }
			at(AvailLocation::scheme.name) { write(scheme.name) }
			at(AvailLocation::path.name) { write(path) }
		}
	}

	override fun equals(other: Any?): Boolean
	{
		if (this === other) return true
		if (other !is AvailLocation) return false

		if (path != other.path) return false

		return true
	}

	override fun hashCode(): Int
	{
		return path.hashCode()
	}

	/**
	 * The acceptable path location types.
	 *
	 * @author Richard Arriaga
	 */
	@Suppress("EnumEntryName")
	enum class LocationType
	{
		/**
		 * Canonical representation of an invalid selection. Should not be used
		 * explicitly in the configuration file; it is only present to handle
		 * error situations with broken config files.
		 */
		invalid
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation =
				InvalidLocation(
					path,
					"Location type is literally $name, which not allowed.")
		},

		/** The path is relative to the [AvailEnvironment.availHome]. */
		availHome
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = AvailHome(path, scheme)
		},

		/** The path is relative to the user's home directory. */
		home
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = UserHome(path, scheme)
		},

		/** The path is relative to the [AvailEnvironment.availHomeLibs]. */
		availLibraries
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = AvailLibraries(path, scheme)
		},

		/** The path is relative to the [AvailEnvironment.availHomeRepos]. */
		availRepositories
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = AvailRepositories(path, scheme)
		},

		/**
		 * The path is relative to the [AvailEnvironment.availHomeWorkbench].
		 */
		availHomeWorkbench
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = AvailHomeWorkbench(path, scheme)
		},

		/** The path is relative to the project root directory. */
		project
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = ProjectHome(path, scheme, pathRelativeSuffix)
		},

		/** The path is relative to [AvailProject.ROOTS_DIR]. */
		projectRoots
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = ProjectRoot(path, scheme, pathRelativeSuffix)
		},

		/** The path is absolute. */
		absolute
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = UserHome(path, scheme)
		};

		/**
		 * Extract a [AvailLocation] of this type from the provided
		 * [scheme].
		 *
		 * @param pathRelativeSuffix
		 *   The path suffix relative to the [AvailLocation].
		 * @param path
		 *   The already extracted path.
		 * @param scheme
		 *   The [JSONObject] to extract the rest of the data from.
		 */
		protected abstract fun location (
			pathRelativeSuffix: String,
			path: String,
			scheme: Scheme
		): AvailLocation

		companion object
		{
			/**
			 * The set of valid names of [LocationType].
			 */
			private val validNames: Set<String> =
				values().map { it.name }.toSet()

			/**
			 * Read a [AvailLocation] from the provided JSON.
			 *
			 * @param projectDirectory
			 *   The path of the root directory of the project.
			 * @param obj
			 *   The [JSONObject] to read from.
			 * @return
			 *   A [AvailLocation]. If there is a problem reading the value, a
			 *   [InvalidLocation] will be answered.
			 */
			fun from (
				projectDirectory: String,
				obj: JSONObject
			): AvailLocation
			{
				val path = try
				{
					obj.getString(AvailLocation::path.name)
				}
				catch (e: Throwable)
				{
					System.err.println(
						"Malformed configuration file: no 'path' " +
							"specified for a Location")
					e.printStackTrace()
					return InvalidLocation(
						"", "missing path")
				}
				val raw = try
				{
					obj.getString(AvailLocation::locationType.name)
				}
				catch (e: Throwable)
				{
					System.err.println(
						"Malformed configuration file: Location missing " +
							"location type")
					e.printStackTrace()
					return InvalidLocation(
						path,
						"missing ${AvailLocation::locationType.name}")
				}
				if (!validNames.contains(raw))
				{
					System.err.println(
						"Malformed configuration file: $raw is not a " +
							"valid ${AvailLocation::locationType.name} value")
					return InvalidLocation(
						path,
						"invalid value for " +
							"${AvailLocation::locationType.name}: $raw")
				}
				val scheme = try
				{
					Scheme.valueOf(obj.getString(AvailLocation::scheme.name))
				}
				catch (e: Throwable)
				{
					System.err.println(
						"Malformed configuration file: Location missing " +
							"scheme type")
					e.printStackTrace()
					return InvalidLocation(
						path,
						"missing ${AvailLocation::scheme.name}")
				}
				return valueOf(raw).location(projectDirectory, path, scheme)
			}
		}
	}

	companion object
	{
		/**
		 * Read a [AvailLocation] from the provided JSON.
		 *
		 * @param projectDirectory
		 *   The path of the root directory of the project.
		 * @param obj
		 *   The [JSONObject] to read from.
		 * @return
		 *   A [AvailLocation]. If there is a problem reading the value, a
		 *   [InvalidLocation] will be answered.
		 */
		fun from (
			projectDirectory: String,
			obj: JSONObject
		): AvailLocation = LocationType.from(projectDirectory, obj)
	}
}

/**
 * The canonical representation of an invalid [AvailLocation].
 *
 * @author Richard Arriaga
 *
 * @property problem
 *   Text explaining the reason the location is invalid.
 *
 * @constructor
 * Construct an [InvalidLocation].
 *
 * @param path
 *   The [AvailLocation.path].
 * @param problem
 *   Text explaining the reason the location is invalid.
 */
class InvalidLocation constructor (
	path: String,
	val problem: String
): AvailLocation(LocationType.invalid, Scheme.INVALID, path)
{
	override val fullPathNoPrefix: String get() = path

	init
	{
		// TODO establish a logs file
	}
}

/**
 * The location that is path relative to the user's home directory.
 *
 * @author Richard Arriaga
 */
open class UserHome constructor (
	path: String,
	scheme: Scheme,
	locationType: LocationType = LocationType.home
): AvailLocation(locationType, scheme, path)
{
	override val fullPathNoPrefix: String get() =
		"${System.getProperty("user.home")}/$path"
}

/**
 * The location that is path relative to [AvailEnvironment.availHome].
 *
 * @author Richard Arriaga
 */
open class AvailHome constructor (
	path: String,
	scheme: Scheme,
	locationType: LocationType = LocationType.availHome
): UserHome(path, scheme, locationType)
{
	override val fullPathNoPrefix: String get() =
		"${AvailEnvironment.availHome}/$path"
}

/**
 * The location that is path relative to [AvailEnvironment.availHome].
 *
 * @author Richard Arriaga
 */
open class AvailLibraries constructor (
	path: String,
	scheme: Scheme
): UserHome(path, scheme, LocationType.availLibraries)
{
	override val fullPathNoPrefix: String get() =
		"${AvailEnvironment.availHomeLibs}/$path"
}

/**
 * The location that is path relative to [AvailEnvironment.availHomeRepos].
 *
 * @author Richard Arriaga
 */
open class AvailRepositories constructor (
	path: String,
	scheme: Scheme
): UserHome(path, scheme, LocationType.availRepositories)
{
	override val fullPathNoPrefix: String get() =
		"${AvailEnvironment.availHomeRepos}/$path"
}

/**
 * The location that is path relative to [AvailEnvironment.availHomeRepos].
 *
 * @author Richard Arriaga
 */
open class AvailHomeWorkbench constructor (
	path: String,
	scheme: Scheme
): UserHome(path, scheme, LocationType.availHomeWorkbench)
{
	override val fullPathNoPrefix: String get() =
		"${AvailEnvironment.availHomeWorkbench}/$path"
}

/**
 * The location that is supplied as an absolute path.
 *
 * @author Richard Arriaga
 */
class Absolute constructor (
	path: String,
	scheme: Scheme
): AvailLocation(LocationType.absolute, scheme, path)
{
	override val fullPathNoPrefix: String get() = path
}

/**
 * The location that is path relative to the project's home directory. By
 * default things that can be edited in this location (e.g. text files) are
 * considered editable by the project.
 *
 * @author Richard Arriaga
 *
 * @property projectHome
 *   The absolute path to the [AvailProject] directory.
 */
open class ProjectHome constructor (
	path: String,
	scheme: Scheme,
	val projectHome: String,
	locationType: LocationType = LocationType.project
): AvailLocation(locationType, scheme, path)
{
	override val fullPathNoPrefix: String get() = "$projectHome/$path"

	override val editable: Boolean = scheme != Scheme.JAR
}

/**
 * The location that is path relative to the project's home
 * [AvailProject.ROOTS_DIR].
 *
 * @author Richard Arriaga
 *
 * @property projectHome
 *   The absolute path to the [AvailProject] directory.
 */
open class ProjectRoot constructor (
	path: String,
	scheme: Scheme,
	projectHome: String
): ProjectHome(path, scheme, projectHome, LocationType.projectRoots)
{
	override val fullPathNoPrefix: String get() =
		"$projectHome/${AvailProject.ROOTS_DIR}/$path"
}
